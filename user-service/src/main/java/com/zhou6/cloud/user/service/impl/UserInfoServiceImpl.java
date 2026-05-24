package com.zhou6.cloud.user.service.impl;

import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.user.dto.UserInfoResponse;
import com.zhou6.cloud.user.dto.VerifyResponse;
import com.zhou6.cloud.user.entity.SysUser;
import com.zhou6.cloud.user.mapper.SysUserMapper;
import com.zhou6.cloud.user.service.UserInfoService;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    private static final short STATUS_ENABLED = 1;
    private static final short LOCKED_NO = 0;

    private final SysUserMapper sysUserMapper;

    public UserInfoServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public VerifyResponse verify(String username, String password) {
        // 登录校验直接查询 sys_user 表，不再使用固定账号或临时缓存数据。
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("limit 1"));
        if (user == null || !isAvailable(user) || !Objects.equals(password, user.getPassword())) {
            return new VerifyResponse(false, "", username, "", "", "");
        }
        return new VerifyResponse(true, String.valueOf(user.getId()), user.getUsername(),
                user.getNickname(), user.getEmail(), user.getContactPhone());
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
        return Objects.equals(user.getStatus(), STATUS_ENABLED) && Objects.equals(user.getIsLocked(), LOCKED_NO);
    }

}
