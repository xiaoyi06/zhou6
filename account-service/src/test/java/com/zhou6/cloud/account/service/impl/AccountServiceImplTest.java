package com.zhou6.cloud.account.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.zhou6.cloud.account.entity.SysAccountFlow;
import com.zhou6.cloud.account.entity.SysAccount;
import com.zhou6.cloud.account.handler.AccountErrorCode;
import com.zhou6.cloud.account.mapper.SysAccountFlowMapper;
import com.zhou6.cloud.account.mapper.SysAccountMapper;
import com.zhou6.cloud.account.service.AccountCacheService;
import com.zhou6.cloud.account.vo.AccountSummaryVO;
import com.zhou6.cloud.common.handler.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountServiceImplTest {

    private SysAccountMapper accountMapper;
    private SysAccountFlowMapper flowMapper;
    private AccountCacheService cacheService;
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountMapper = mock(SysAccountMapper.class);
        flowMapper = mock(SysAccountFlowMapper.class);
        cacheService = mock(AccountCacheService.class);
        accountService = new AccountServiceImpl(accountMapper, flowMapper, cacheService);
    }

    @Test
    void freezeRejectsAmountWithMoreThanFourDecimalPlacesBeforeTouchingCache() {
        BizException ex = assertThrows(BizException.class,
                () -> accountService.freeze(10001L, new BigDecimal("1.00001"), "ORDER_PAY", "order-1"));

        assertEquals(AccountErrorCode.AMOUNT_INVALID.getCode(), ex.getCode());
        verify(cacheService, never()).tryFreezeLock(any(), any());
    }

    @Test
    void freezeReturnsBalanceNotEnoughWhenRedisAtomicCheckFails() {
        when(cacheService.tryFreezeLock("ORDER_PAY", "order-2")).thenReturn(true);
        when(cacheService.getSummary(10001L)).thenReturn(summary());
        when(cacheService.freeze(eq(10001L), eq(new BigDecimal("1.0000")))).thenReturn(-1L);

        BizException ex = assertThrows(BizException.class,
                () -> accountService.freeze(10001L, new BigDecimal("1.0000"), "ORDER_PAY", "order-2"));

        assertEquals(AccountErrorCode.BALANCE_NOT_ENOUGH.getCode(), ex.getCode());
        verify(accountMapper, never()).freezeAvailable(any(), any(), any());
        verify(cacheService).unlockFreeze("ORDER_PAY", "order-2");
    }

    @Test
    void freezeRefreshesCacheWhenDatabaseSnapshotUpdateFails() {
        SysAccount account = account(10001L);
        when(cacheService.tryFreezeLock("ORDER_PAY", "order-3")).thenReturn(true);
        when(cacheService.getSummary(10001L)).thenReturn(summary());
        when(cacheService.freeze(eq(10001L), eq(new BigDecimal("1.0000")))).thenReturn(1L);
        when(accountMapper.freezeAvailable(10001L, new BigDecimal("1.0000"), 10001L)).thenReturn(0);
        when(accountMapper.selectOne(any(Wrapper.class))).thenReturn(account);

        BizException ex = assertThrows(BizException.class,
                () -> accountService.freeze(10001L, new BigDecimal("1.0000"), "ORDER_PAY", "order-3"));

        assertEquals(AccountErrorCode.BALANCE_NOT_ENOUGH.getCode(), ex.getCode());
        verify(cacheService).writeSnapshot(account);
        verify(cacheService).unlockFreeze("ORDER_PAY", "order-3");
    }

    @Test
    void creditFailsWhenAccountSnapshotCannotBeUpdated() {
        SysAccount account = account(10001L);
        when(accountMapper.selectOne(any(Wrapper.class))).thenReturn(account);
        when(flowMapper.insert(any(SysAccountFlow.class))).thenReturn(1);
        when(accountMapper.creditAvailable(10001L, new BigDecimal("2.0000"), 0L)).thenReturn(0);

        BizException ex = assertThrows(BizException.class,
                () -> accountService.credit(10001L, new BigDecimal("2.0000"), "REWARD", "reward-1", null, "test"));

        assertEquals(AccountErrorCode.ACCOUNT_NOT_FOUND.getCode(), ex.getCode());
        verify(cacheService, never()).writeSnapshot(account);
    }

    @Test
    void freezeSucceedsWhenRedisAndDatabaseBothSucceed() {
        when(cacheService.tryFreezeLock("ORDER_PAY", "order-4")).thenReturn(true);
        when(cacheService.getSummary(10001L)).thenReturn(summary());
        when(cacheService.freeze(eq(10001L), eq(new BigDecimal("1.0000")))).thenReturn(1L);
        when(accountMapper.freezeAvailable(10001L, new BigDecimal("1.0000"), 10001L)).thenReturn(1);

        boolean result = accountService.freeze(10001L, new BigDecimal("1.0000"), "ORDER_PAY", "order-4");

        assertTrue(result);
        verify(cacheService).unlockFreeze("ORDER_PAY", "order-4");
    }

    @Test
    void reverseOriginalOutFlowRestoresAvailableWithoutIncreasingTotalAmount() {
        SysAccountFlow original = new SysAccountFlow();
        original.setUserId(10001L);
        original.setDirection((short) 2);
        original.setAmount(new BigDecimal("3.0000"));
        SysAccount account = account(10001L);
        when(flowMapper.selectOne(any(Wrapper.class))).thenReturn(original);
        when(flowMapper.insert(any(SysAccountFlow.class))).thenReturn(1);
        when(accountMapper.restoreAvailable(10001L, new BigDecimal("3.0000"), 9L)).thenReturn(1);
        when(accountMapper.selectOne(any(Wrapper.class))).thenReturn(account);

        accountService.reverse("ORDER_PAY", "order-5", "ORDER_REFUND", "order-5", 9L);

        verify(accountMapper).restoreAvailable(10001L, new BigDecimal("3.0000"), 9L);
        verify(accountMapper, never()).creditAvailable(10001L, new BigDecimal("3.0000"), 9L);
        verify(cacheService).writeSnapshot(account);
    }

    private AccountSummaryVO summary() {
        AccountSummaryVO summary = new AccountSummaryVO();
        summary.setAvailable(new BigDecimal("10.0000"));
        summary.setFrozen(BigDecimal.ZERO.setScale(4));
        summary.setSettling(BigDecimal.ZERO.setScale(4));
        summary.setTotal(new BigDecimal("10.0000"));
        return summary;
    }

    private SysAccount account(Long userId) {
        SysAccount account = new SysAccount();
        account.setId(1L);
        account.setUserId(userId);
        account.setAvailableAmount(new BigDecimal("10.0000"));
        account.setFrozenAmount(BigDecimal.ZERO.setScale(4));
        account.setSettlingAmount(BigDecimal.ZERO.setScale(4));
        account.setTotalAmount(new BigDecimal("10.0000"));
        account.setVersion(0);
        account.setCreateBy(0L);
        account.setCreateTime(LocalDateTime.now());
        account.setUpdateBy(0L);
        account.setUpdateTime(LocalDateTime.now());
        return account;
    }
}
