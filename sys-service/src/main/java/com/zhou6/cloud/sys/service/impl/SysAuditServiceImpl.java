package com.zhou6.cloud.sys.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.zhou6.cloud.sys.dto.LoginLogDTO;
import com.zhou6.cloud.sys.dto.LoginLogQueryDTO;
import com.zhou6.cloud.sys.entity.SysLoginLog;
import com.zhou6.cloud.sys.mapper.SysLoginLogMapper;
import com.zhou6.cloud.sys.service.SysAuditService;
import com.zhou6.cloud.sys.vo.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SysAuditServiceImpl extends BaseSysService implements SysAuditService {

    private static final Logger log = LoggerFactory.getLogger(SysAuditServiceImpl.class);

    private final BlockingQueue<SysLoginLog> loginLogQueue = new ArrayBlockingQueue<>(5000);
    private final JdbcTemplate jdbcTemplate;
    private final SysLoginLogMapper loginLogMapper;
    private final int batchSize;

    public SysAuditServiceImpl(JdbcTemplate jdbcTemplate, SysLoginLogMapper loginLogMapper,
            @Value("${zhou6.sys.login-log.batch-size:100}") int batchSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.loginLogMapper = loginLogMapper;
        this.batchSize = batchSize;
    }

    @Override
    public void publishLoginLog(LoginLogDTO dto) {
        SysLoginLog logEntity = toEntity(dto);
        boolean offered = loginLogQueue.offer(logEntity);
        if (!offered) {
            log.warn("Login log queue is full, drop audit event: username={}, userId={}",
                    logEntity.getUsername(), logEntity.getUserId());
        }
    }

    @Override
    public PageResponse<SysLoginLog> loginLogPage(LoginLogQueryDTO dto) {
        LoginLogQueryDTO query = dto == null ? new LoginLogQueryDTO() : dto;
        long pageNum = pageNum(query.getPageNum());
        long pageSize = pageSize(query.getPageSize());
        String username = hasText(query.getUsername()) ? query.getUsername() : null;
        String beginTime = hasText(query.getBeginTime()) ? query.getBeginTime() : null;
        String endTime = hasText(query.getEndTime()) ? query.getEndTime() : null;
        return new PageResponse<>(loginLogMapper.count(username, query.getStatus(), beginTime, endTime), pageNum, pageSize,
                loginLogMapper.selectPage(username, query.getStatus(), beginTime, endTime,
                        pageSize, (pageNum - 1) * pageSize));
    }

    @Override
    public void flushLoginLogs() {
        List<SysLoginLog> logs = new ArrayList<>(batchSize);
        loginLogQueue.drainTo(logs, batchSize);
        if (logs.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate("""
                INSERT INTO sys_login_log (user_id, username, ip_address, login_location, browser, os, status, msg, login_time)
                VALUES (?, ?, CAST(? AS inet), ?, ?, ?, ?, ?, ?)
                """, logs, logs.size(), (ps, item) -> {
            ps.setObject(1, item.getUserId());
            ps.setString(2, item.getUsername());
            ps.setString(3, hasText(item.getIpAddress()) ? item.getIpAddress() : "0.0.0.0");
            ps.setString(4, item.getLoginLocation());
            ps.setString(5, item.getBrowser());
            ps.setString(6, item.getOs());
            ps.setShort(7, item.getStatus() == null ? 0 : item.getStatus());
            ps.setString(8, item.getMsg());
            ps.setTimestamp(9, Timestamp.valueOf(item.getLoginTime()));
        });
    }

    private SysLoginLog toEntity(LoginLogDTO dto) {
        require(dto != null, "登录日志参数不能为空");
        SysLoginLog entity = new SysLoginLog();
        entity.setUserId(parseNullableId(dto.getUserId(), "用户ID不正确"));
        entity.setUsername(dto.getUsername());
        entity.setIpAddress(dto.getIpAddress());
        entity.setLoginLocation(dto.getLoginLocation());
        entity.setBrowser(dto.getBrowser());
        entity.setOs(dto.getOs());
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus().shortValue());
        entity.setMsg(dto.getMsg());
        entity.setLoginTime(LocalDateTime.now());
        return entity;
    }
}
