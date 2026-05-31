package com.zhou6.cloud.user.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhou6.cloud.common.constant.StatusConstants;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import com.zhou6.cloud.user.client.FileClient;
import com.zhou6.cloud.user.dto.FileIdRequest;
import com.zhou6.cloud.user.dto.UserAvatarDTO;
import com.zhou6.cloud.user.dto.UserChangePasswordDTO;
import com.zhou6.cloud.user.dto.UserInfoResponse;
import com.zhou6.cloud.user.dto.VerifyResponse;
import com.zhou6.cloud.user.entity.SysUser;
import com.zhou6.cloud.user.mapper.SysUserMapper;
import com.zhou6.cloud.user.service.UserInfoService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 2;

    private final SysUserMapper sysUserMapper;
    private final FileClient fileClient;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserInfoServiceImpl(SysUserMapper sysUserMapper, FileClient fileClient) {
        this.sysUserMapper = sysUserMapper;
        this.fileClient = fileClient;
    }

    @Override
    public VerifyResponse verify(String username, String password, String loginIp) {
        // 登录校验直接查询 sys_user 表，不再使用固定账号或临时缓存数据。
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));
        if (user == null) {
            return failed(username, CommonErrorCode.LOGIN_FAILED.getMessage());
        }
        if (!Objects.equals(user.getStatus(), StatusConstants.STATUS_ENABLED)) {
            return failed(username, CommonErrorCode.ACCOUNT_DISABLED.getMessage());
        }
        LocalDateTime now = LocalDateTime.now();
        if (isLocked(user)) {
            if (!isLockExpired(user, now)) {
                return failed(username, CommonErrorCode.ACCOUNT_LOCKED.getMessage());
            }
            unlockUser(user);
        }
        if (!passwordMatches(password, user.getPassword())) {
            String message = recordFailedLogin(user, now);
            return failed(username, message);
        }
        resetLoginFailure(user);
        recordLoginSuccess(user, loginIp, now);
        return new VerifyResponse(true, String.valueOf(user.getId()), user.getUsername(),
                user.getNickname(), user.getEmail(), user.getContactPhone(), "");
    }

    @Override
    public UserInfoResponse getCurrentUserInfo() {
        // 用户基础信息直接从 sys_user 表读取，避免接口返回临时模拟数据。
        Long id = UserContextHolder.getUserId();
        if (id == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || !isAvailable(user)) {
            return null;
        }
        UserInfoResponse response = new UserInfoResponse(String.valueOf(user.getId()), user.getUsername(),
                user.getNickname(), user.getEmail(), user.getContactPhone());
        response.setAvatarFileId(user.getAvatarFileId() == null ? null : String.valueOf(user.getAvatarFileId()));
        return response;
    }

    /**
     * 修改当前登录用户头像。头像文件必须已经通过 file-service 上传，并且对象存储中真实存在。
     *
     * @param dto 头像文件参数
     */
    @Override
    public void updateCurrentUserAvatar(UserAvatarDTO dto) {
        require(dto != null && hasText(dto.getAvatarFileId()), "头像文件ID不能为空");
        Long currentUserId = UserContextHolder.getUserId();
        require(currentUserId != null, "当前登录用户不存在");
        SysUser user = sysUserMapper.selectById(currentUserId);
        require(user != null && isAvailable(user), "当前登录用户不存在");
        Long avatarFileId = parseRequiredId(dto.getAvatarFileId(), "头像文件ID不正确");
        requireFileExists(avatarFileId);
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, currentUserId)
                .set(SysUser::getAvatarFileId, avatarFileId));
    }

    /**
     * 当前登录用户修改自己的密码；必须先校验旧密码，避免仅凭用户 ID 修改密码。
     *
     * @param dto 修改密码参数
     */
    @Override
    public void changeCurrentUserPassword(UserChangePasswordDTO dto) {
        require(dto != null, "密码参数不能为空");
        require(hasText(dto.getOldPassword()), "旧密码不能为空");
        require(hasText(dto.getNewPassword()), "新密码不能为空");
        Long currentUserId = UserContextHolder.getUserId();
        require(currentUserId != null, "当前登录用户不存在");
        SysUser user = sysUserMapper.selectById(currentUserId);
        require(user != null && isAvailable(user), "当前登录用户不存在");
        require(passwordMatches(dto.getOldPassword(), user.getPassword()), "旧密码不正确");
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, currentUserId)
                .set(SysUser::getPassword, passwordEncoder.encode(dto.getNewPassword()))
                .set(SysUser::getFailedLoginAttempts, 0)
                .set(SysUser::getLockedUntil, null)
                .set(SysUser::getIsLocked, StatusConstants.LOCKED_NO));
    }

    private boolean isAvailable(SysUser user) {
        return Objects.equals(user.getStatus(), StatusConstants.STATUS_ENABLED) && Objects.equals(user.getIsLocked(), StatusConstants.LOCKED_NO);
    }

    private boolean isLocked(SysUser user) {
        return Objects.equals(user.getIsLocked(), StatusConstants.LOCKED_YES);
    }

    private boolean isLockExpired(SysUser user, LocalDateTime now) {
        LocalDateTime lockedUntil = user.getLockedUntil();
        return lockedUntil != null && !lockedUntil.isAfter(now);
    }

    private String recordFailedLogin(SysUser user, LocalDateTime now) {
        int failedAttempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        int nextFailedAttempts = failedAttempts + 1;
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getFailedLoginAttempts, nextFailedAttempts);
        String message = CommonErrorCode.LOGIN_FAILED.getMessage();
        if (nextFailedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
            updateWrapper
                    .set(SysUser::getIsLocked, StatusConstants.LOCKED_YES)
                    .set(SysUser::getLockedUntil, now.plusMinutes(LOCK_MINUTES));
            message = CommonErrorCode.ACCOUNT_LOCKED.getMessage();
        }
        sysUserMapper.update(null, updateWrapper);
        return message;
    }

    private void unlockUser(SysUser user) {
        user.setIsLocked(StatusConstants.LOCKED_NO);
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        resetLoginFailure(user);
    }

    private void resetLoginFailure(SysUser user) {
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getIsLocked, StatusConstants.LOCKED_NO)
                .set(SysUser::getFailedLoginAttempts, 0)
                .set(SysUser::getLockedUntil, null));
    }

    private void recordLoginSuccess(SysUser user, String loginIp, LocalDateTime now) {
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getLastLoginIp, loginIp)
                .set(SysUser::getLastLoginTime, now));
    }

    private VerifyResponse failed(String username, String message) {
        return new VerifyResponse(false, "", username, "", "", "", message);
    }

    /**
     * 保存头像前校验文件是否存在，避免用户表保存无效文件 ID。
     */
    private void requireFileExists(Long fileId) {
        R<Boolean> response = fileClient.exists(new FileIdRequest(String.valueOf(fileId)));
        require(response != null && response.success() && Boolean.TRUE.equals(response.getData()), "头像文件不存在");
    }

    /**
     * 将请求中的文件 ID 转为 Long，保持 sys_user.avatar_file_id 与 sys_file.id 类型一致。
     */
    private Long parseRequiredId(String value, String message) {
        require(hasText(value), message);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    private void require(boolean expression, String message) {
        if (!expression) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, message);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return Objects.equals(rawPassword, storedPassword);
    }

}
