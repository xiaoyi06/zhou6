package com.zhou6.cloud.account.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhou6.cloud.account.entity.SysAccount;
import com.zhou6.cloud.account.entity.SysAccountFlow;
import com.zhou6.cloud.account.handler.AccountErrorCode;
import com.zhou6.cloud.account.mapper.SysAccountFlowMapper;
import com.zhou6.cloud.account.mapper.SysAccountMapper;
import com.zhou6.cloud.account.service.AccountCacheService;
import com.zhou6.cloud.account.service.AccountService;
import com.zhou6.cloud.account.vo.AccountSummaryVO;
import com.zhou6.cloud.common.handler.BizException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账户业务服务实现，协调 Redis 原子变更、PostgreSQL 快照更新和流水审计。
 */
@Service
public class AccountServiceImpl implements AccountService {

    private static final short DIRECTION_IN = 1;
    private static final short DIRECTION_OUT = 2;
    private static final short TARGET_AVAILABLE = 1;
    private static final short TARGET_FROZEN = 2;
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int MONEY_SCALE = 4;
    private static final int MONEY_PRECISION = 16;
    private static final int BIZ_TYPE_MAX_LENGTH = 50;
    private static final int BIZ_ID_MAX_LENGTH = 64;
    private static final int REMARK_MAX_LENGTH = 255;

    private final SysAccountMapper accountMapper;
    private final SysAccountFlowMapper flowMapper;
    private final AccountCacheService cacheService;

    public AccountServiceImpl(SysAccountMapper accountMapper, SysAccountFlowMapper flowMapper,
            AccountCacheService cacheService) {
        this.accountMapper = accountMapper;
        this.flowMapper = flowMapper;
        this.cacheService = cacheService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountSummaryVO summary(Long userId) {
        requireUserId(userId);
        AccountSummaryVO cached = cacheService.getSummary(userId);
        if (cached != null) {
            return cached;
        }
        String initLockValue = cacheService.tryInitLock(userId);
        if (initLockValue != null) {
            try {
                SysAccount account = getOrCreateAccount(userId);
                cacheService.writeSnapshot(account);
                return cacheService.getSummary(userId);
            } finally {
                cacheService.unlockInit(userId, initLockValue);
            }
        }
        SysAccount account = getOrCreateAccount(userId);
        cacheService.writeSnapshot(account);
        return cacheService.getSummary(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean freeze(Long userId, BigDecimal amount, String bizType, String bizId) {
        requireUserId(userId);
        amount = requireAmount(amount);
        requireBiz(bizType, bizId);
        String freezeLockValue = cacheService.tryFreezeLock(bizType, bizId);
        if (freezeLockValue == null) {
            throw new BizException(AccountErrorCode.BIZ_ID_DUPLICATED);
        }
        try {
            summary(userId);
            long result = cacheService.freeze(userId, amount);
            if (result == -1L) {
                throw new BizException(AccountErrorCode.BALANCE_NOT_ENOUGH);
            }
            int updated = accountMapper.freezeAvailable(userId, amount, userId);
            if (updated == 0) {
                refreshCache(userId);
                throw new BizException(AccountErrorCode.BALANCE_NOT_ENOUGH);
            }
            return result == 1L;
        } finally {
            cacheService.unlockFreeze(bizType, bizId, freezeLockValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleFrozen(Long userId, BigDecimal amount, String bizType, String bizId, Long operatorId, String remark) {
        requireUserId(userId);
        amount = requireAmount(amount);
        requireBiz(bizType, bizId);
        remark = safeRemark(remark);
        Long op = operatorId(operatorId);
        insertFlow(userId, DIRECTION_OUT, TARGET_FROZEN, amount, bizType, bizId, op, remark);
        int updated = accountMapper.settleFrozen(userId, amount, op);
        if (updated == 0) {
            throw new BizException(AccountErrorCode.FROZEN_BALANCE_NOT_ENOUGH);
        }
        refreshCache(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfreeze(Long userId, BigDecimal amount, String bizType, String bizId, Long operatorId, String remark) {
        requireUserId(userId);
        amount = requireAmount(amount);
        requireBiz(bizType, bizId);
        remark = safeRemark(remark);
        Long op = operatorId(operatorId);
        insertFlow(userId, DIRECTION_IN, TARGET_AVAILABLE, amount, bizType, bizId, op, remark);
        try {
            summary(userId);
            long redisResult = cacheService.unfreeze(userId, amount);
            if (redisResult == -1L) {
                throw new BizException(AccountErrorCode.FROZEN_BALANCE_NOT_ENOUGH);
            }
            int updated = accountMapper.unfreeze(userId, amount, op);
            if (updated == 0) {
                refreshCache(userId);
                throw new BizException(AccountErrorCode.FROZEN_BALANCE_NOT_ENOUGH);
            }
            refreshCache(userId);
        } catch (RuntimeException ex) {
            refreshCache(userId);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void credit(Long userId, BigDecimal amount, String bizType, String bizId, Long operatorId, String remark) {
        requireUserId(userId);
        amount = requireAmount(amount);
        requireBiz(bizType, bizId);
        remark = safeRemark(remark);
        Long op = operatorId(operatorId);
        getOrCreateAccount(userId);
        insertFlow(userId, DIRECTION_IN, TARGET_AVAILABLE, amount, bizType, bizId, op, remark);
        int updated = accountMapper.creditAvailable(userId, amount, op);
        if (updated == 0) {
            throw new BizException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }
        refreshCache(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void debit(Long userId, BigDecimal amount, String bizType, String bizId, Long operatorId, String remark) {
        requireUserId(userId);
        amount = requireAmount(amount);
        requireBiz(bizType, bizId);
        remark = safeRemark(remark);
        Long op = operatorId(operatorId);
        summary(userId);
        insertFlow(userId, DIRECTION_OUT, TARGET_AVAILABLE, amount, bizType, bizId, op, remark);
        try {
            long redisResult = cacheService.debit(userId, amount);
            if (redisResult == -1L) {
                throw new BizException(AccountErrorCode.BALANCE_NOT_ENOUGH);
            }
            int updated = accountMapper.debitAvailable(userId, amount, op);
            if (updated == 0) {
                refreshCache(userId);
                throw new BizException(AccountErrorCode.BALANCE_NOT_ENOUGH);
            }
            refreshCache(userId);
        } catch (RuntimeException ex) {
            refreshCache(userId);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reverse(String origBizType, String origBizId, String reverseBizType, String reverseBizId, Long adminId) {
        requireBiz(origBizType, origBizId);
        requireBiz(reverseBizType, reverseBizId);
        Long op = operatorId(adminId);
        SysAccountFlow original = flowMapper.selectOne(new LambdaQueryWrapper<SysAccountFlow>()
                .eq(SysAccountFlow::getBizType, origBizType)
                .eq(SysAccountFlow::getBizId, origBizId));
        if (original == null) {
            throw new BizException(AccountErrorCode.ORIGINAL_FLOW_NOT_FOUND);
        }
        if (original.getDirection() == DIRECTION_IN) {
            debit(original.getUserId(), original.getAmount(), reverseBizType, reverseBizId, op, "系统红字冲正");
        } else {
            restoreAvailable(original.getUserId(), original.getAmount(), reverseBizType, reverseBizId, op, "系统红字冲正");
        }
    }

    private void restoreAvailable(Long userId, BigDecimal amount, String bizType, String bizId, Long operatorId,
            String remark) {
        insertFlow(userId, DIRECTION_IN, TARGET_AVAILABLE, amount, bizType, bizId, operatorId, remark);
        int updated = accountMapper.restoreAvailable(userId, amount, operatorId);
        if (updated == 0) {
            throw new BizException(AccountErrorCode.ACCOUNT_NOT_FOUND);
        }
        refreshCache(userId);
    }

    private SysAccount getOrCreateAccount(Long userId) {
        SysAccount account = findByUserId(userId);
        if (account != null) {
            return account;
        }
        SysAccount created = new SysAccount();
        created.setUserId(userId);
        created.setAvailableAmount(ZERO);
        created.setFrozenAmount(ZERO);
        created.setSettlingAmount(ZERO);
        created.setTotalAmount(ZERO);
        created.setVersion(0);
        created.setCreateBy(0L);
        created.setUpdateBy(0L);
        created.setCreateTime(LocalDateTime.now());
        created.setUpdateTime(LocalDateTime.now());
        try {
            accountMapper.insert(created);
            return created;
        } catch (DuplicateKeyException ex) {
            SysAccount existing = findByUserId(userId);
            if (existing == null) {
                throw new BizException(AccountErrorCode.ACCOUNT_CONSISTENCY_ERROR, "账户开户并发冲突后未查询到账户", ex);
            }
            return existing;
        }
    }

    private SysAccount findByUserId(Long userId) {
        return accountMapper.selectOne(new LambdaQueryWrapper<SysAccount>().eq(SysAccount::getUserId, userId));
    }

    private void insertFlow(Long userId, short direction, short targetType, BigDecimal amount, String bizType,
            String bizId, Long operatorId, String remark) {
        SysAccountFlow flow = new SysAccountFlow();
        flow.setUserId(userId);
        flow.setDirection(direction);
        flow.setTargetType(targetType);
        flow.setAmount(amount);
        flow.setBizType(bizType);
        flow.setBizId(bizId);
        flow.setRemark(remark);
        flow.setCreateBy(operatorId);
        flow.setCreateTime(LocalDateTime.now());
        try {
            flowMapper.insert(flow);
        } catch (DuplicateKeyException ex) {
            throw new BizException(AccountErrorCode.BIZ_ID_DUPLICATED, AccountErrorCode.BIZ_ID_DUPLICATED.getMessage(), ex);
        }
    }

    private void refreshCache(Long userId) {
        SysAccount account = findByUserId(userId);
        if (account != null) {
            cacheService.writeSnapshot(account);
        }
    }

    private void requireUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be positive");
        }
    }

    private BigDecimal requireAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new BizException(AccountErrorCode.AMOUNT_INVALID);
        }
        BigDecimal normalized;
        try {
            normalized = amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new BizException(AccountErrorCode.AMOUNT_INVALID, "金额最多支持4位小数", ex);
        }
        if (normalized.precision() > MONEY_PRECISION) {
            throw new BizException(AccountErrorCode.AMOUNT_INVALID, "金额整数和小数总位数不能超过16位");
        }
        return normalized;
    }

    private void requireBiz(String bizType, String bizId) {
        if (bizType == null || bizType.isBlank() || bizType.length() > BIZ_TYPE_MAX_LENGTH
                || bizId == null || bizId.isBlank() || bizId.length() > BIZ_ID_MAX_LENGTH) {
            throw new BizException(AccountErrorCode.BIZ_PARAM_INVALID);
        }
    }

    private Long operatorId(Long operatorId) {
        if (operatorId == null) {
            return 0L;
        }
        if (operatorId < 0) {
            throw new BizException(AccountErrorCode.BIZ_PARAM_INVALID);
        }
        return operatorId;
    }

    private String safeRemark(String remark) {
        if (remark != null && remark.length() > REMARK_MAX_LENGTH) {
            throw new BizException(AccountErrorCode.BIZ_PARAM_INVALID, "备注长度不能超过255个字符");
        }
        return remark;
    }
}
