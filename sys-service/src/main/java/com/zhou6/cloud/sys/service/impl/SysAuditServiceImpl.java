package com.zhou6.cloud.sys.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.zhou6.cloud.sys.dto.LoginLogDTO;
import com.zhou6.cloud.sys.dto.LoginLogBatchDeleteDTO;
import com.zhou6.cloud.sys.dto.LoginLogDeleteDTO;
import com.zhou6.cloud.sys.dto.LoginLogDeleteParam;
import com.zhou6.cloud.sys.dto.LoginLogQueryDTO;
import com.zhou6.cloud.sys.entity.SysLoginLog;
import com.zhou6.cloud.sys.mapper.SysLoginLogMapper;
import com.zhou6.cloud.sys.service.SysAuditService;
import com.zhou6.cloud.sys.vo.PageResponse;
import com.zhou6.cloud.sys.vo.LoginLogVO;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.common.handler.CommonErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysAuditServiceImpl extends BaseSysService implements SysAuditService {

    private static final Logger log = LoggerFactory.getLogger(SysAuditServiceImpl.class);

    private final BlockingQueue<SysLoginLog> loginLogQueue = new ArrayBlockingQueue<>(5000);
    private final SysLoginLogMapper loginLogMapper;
    private final int batchSize;

    public SysAuditServiceImpl(SysLoginLogMapper loginLogMapper,
            @Value("${zhou6.sys.login-log.batch-size:100}") int batchSize) {
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
    public PageResponse<LoginLogVO> loginLogPage(LoginLogQueryDTO dto) {
        LoginLogQueryDTO query = dto == null ? new LoginLogQueryDTO() : dto;
        long pageNum = pageNum(query.getPageNum());
        long pageSize = pageSize(query.getPageSize());
        String username = hasText(query.getUsername()) ? query.getUsername() : null;
        String beginTime = hasText(query.getBeginTime()) ? query.getBeginTime() : null;
        String endTime = hasText(query.getEndTime()) ? query.getEndTime() : null;
        return new PageResponse<>(loginLogMapper.count(username, query.getStatus(), beginTime, endTime), pageNum, pageSize,
                loginLogMapper.selectPage(username, query.getStatus(), beginTime, endTime,
                        pageSize, (pageNum - 1) * pageSize).stream().map(this::toVo).toList());
    }

    @Override
    public void deleteLoginLog(LoginLogDeleteDTO dto) {
        LoginLogDeleteParam record = toDeleteParam(dto);
        require(loginLogMapper.deleteByIdAndLoginTime(record.id(), record.loginTime()) > 0, "登录日志不存在或已删除");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLoginLogs(LoginLogBatchDeleteDTO dto) {
        require(dto != null && dto.getLogs() != null && !dto.getLogs().isEmpty(), "删除登录日志参数不能为空");
        require(dto.getLogs().size() <= 100, "一次最多删除100条登录日志");
        List<LoginLogDeleteParam> records = dto.getLogs().stream().map(this::toDeleteParam).toList();
        require(loginLogMapper.deleteByRecords(records) == records.size(), "部分登录日志不存在或已删除");
    }

    @Override
    public void flushLoginLogs() {
        List<SysLoginLog> logs = new ArrayList<>(batchSize);
        loginLogQueue.drainTo(logs, batchSize);
        if (logs.isEmpty()) {
            return;
        }
        try {
            loginLogMapper.insertBatch(logs);
        } catch (RuntimeException ex) {
            int requeued = 0;
            for (SysLoginLog item : logs) {
                if (loginLogQueue.offer(item)) {
                    requeued++;
                }
            }
            log.error("Failed to persist login logs; requeued {}/{} events", requeued, logs.size(), ex);
        }
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

    private LoginLogVO toVo(SysLoginLog entity) {
        LoginLogVO vo = new LoginLogVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId() == null ? null : String.valueOf(entity.getUserId()));
        vo.setUsername(entity.getUsername());
        vo.setIpAddress(entity.getIpAddress());
        vo.setLoginLocation(entity.getLoginLocation());
        vo.setBrowser(entity.getBrowser());
        vo.setOs(entity.getOs());
        vo.setStatus(entity.getStatus());
        vo.setMsg(entity.getMsg());
        vo.setLoginTime(entity.getLoginTime());
        return vo;
    }

    private LoginLogDeleteParam toDeleteParam(LoginLogDeleteDTO dto) {
        require(dto != null, "删除登录日志参数不能为空");
        Long id = parseRequiredId(dto.getId(), "登录日志ID不能为空");
        require(hasText(dto.getLoginTime()), "登录日志时间不能为空");
        try {
            return new LoginLogDeleteParam(id, LocalDateTime.parse(dto.getLoginTime().replace(' ', 'T')));
        } catch (RuntimeException ex) {
            throw new BizException(CommonErrorCode.PARAM_INVALID, "登录日志时间格式不正确");
        }
    }
}
