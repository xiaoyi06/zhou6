package com.zhou6.cloud.user.service.impl;

import java.time.LocalDateTime;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhou6.cloud.common.constant.StatusConstants;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.common.handler.CommonErrorCode;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserInfoServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public VerifyResponse verify(String username, String password) {
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
        return new UserInfoResponse(String.valueOf(user.getId()), user.getUsername(),
                user.getNickname(), user.getEmail(), user.getContactPhone());
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

    private VerifyResponse failed(String username, String message) {
        return new VerifyResponse(false, "", username, "", "", "", message);
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
