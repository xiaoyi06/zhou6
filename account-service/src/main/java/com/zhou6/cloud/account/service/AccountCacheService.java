package com.zhou6.cloud.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.zhou6.cloud.account.entity.SysAccount;
import com.zhou6.cloud.account.handler.AccountErrorCode;
import com.zhou6.cloud.account.vo.AccountSummaryVO;
import com.zhou6.cloud.common.handler.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

/**
 * 账户 Redis 缓存服务，封装现金快照 Hash 读写和 Lua 原子变更脚本。
 */
@Service
public class AccountCacheService {

    private static final String AVAILABLE = "available";
    private static final String FROZEN = "frozen";
    private static final String SETTLING = "settling";
    private static final String TOTAL = "total";

    private final StringRedisTemplate redisTemplate;
    private final Duration cacheTtl;
    private final DefaultRedisScript<Long> freezeScript;
    private final DefaultRedisScript<Long> unfreezeScript;
    private final DefaultRedisScript<Long> creditScript;
    private final DefaultRedisScript<Long> debitScript;

    public AccountCacheService(StringRedisTemplate redisTemplate,
            @Value("${zhou6.account.cache-ttl-hours:24}") long cacheTtlHours) {
        this.redisTemplate = redisTemplate;
        this.cacheTtl = Duration.ofHours(cacheTtlHours);
        this.freezeScript = loadScript("lua/freeze.lua");
        this.unfreezeScript = loadScript("lua/unfreeze.lua");
        this.creditScript = loadScript("lua/credit.lua");
        this.debitScript = loadScript("lua/debit.lua");
    }

    /**
     * 从 Redis 读取账户现金汇总。
     *
     * @param userId 用户 ID
     * @return 缓存命中时返回现金汇总，未命中返回 null
     */
    public AccountSummaryVO getSummary(Long userId) {
        Map<Object, Object> values = redisTemplate.opsForHash().entries(accountKey(userId));
        if (values.isEmpty()) {
            return null;
        }
        return toSummary(values);
    }

    /**
     * 将数据库账户快照写入 Redis Hash。
     *
     * @param account 账户快照
     */
    public void writeSnapshot(SysAccount account) {
        String key = accountKey(account.getUserId());
        redisTemplate.opsForHash().put(key, AVAILABLE, money(account.getAvailableAmount()));
        redisTemplate.opsForHash().put(key, FROZEN, money(account.getFrozenAmount()));
        redisTemplate.opsForHash().put(key, SETTLING, money(account.getSettlingAmount()));
        redisTemplate.opsForHash().put(key, TOTAL, money(account.getTotalAmount()));
        redisTemplate.expire(key, cacheTtl);
    }

    /**
     * 尝试获取账户初始化锁，防止缓存冷启动时多个线程同时穿透数据库。
     *
     * @param userId 用户 ID
     * @return true 表示获取锁成功
     */
    public boolean tryInitLock(Long userId) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(initLockKey(userId), "1", Duration.ofSeconds(10));
        return Boolean.TRUE.equals(locked);
    }

    /**
     * 释放账户初始化锁。
     *
     * @param userId 用户 ID
     */
    public void unlockInit(Long userId) {
        redisTemplate.delete(initLockKey(userId));
    }

    /**
     * 尝试获取冻结业务防重锁。
     *
     * @param bizType 业务类型
     * @param bizId 业务唯一号
     * @return true 表示获取锁成功
     */
    public boolean tryFreezeLock(String bizType, String bizId) {
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(freezeLockKey(bizType, bizId), "1", Duration.ofSeconds(10));
        return Boolean.TRUE.equals(locked);
    }

    /**
     * 释放冻结业务防重锁。
     *
     * @param bizType 业务类型
     * @param bizId 业务唯一号
     */
    public void unlockFreeze(String bizType, String bizId) {
        redisTemplate.delete(freezeLockKey(bizType, bizId));
    }

    /**
     * 执行预冻结 Lua 脚本。
     *
     * @param userId 用户 ID
     * @param amount 冻结金额
     * @return 1 表示成功，-1 表示余额不足
     */
    public long freeze(Long userId, BigDecimal amount) {
        return execute(freezeScript, userId, amount);
    }

    /**
     * 执行解冻 Lua 脚本。
     *
     * @param userId 用户 ID
     * @param amount 解冻金额
     * @return 1 表示成功，-1 表示冻结余额不足
     */
    public long unfreeze(Long userId, BigDecimal amount) {
        return execute(unfreezeScript, userId, amount);
    }

    /**
     * 执行入账 Lua 脚本。
     *
     * @param userId 用户 ID
     * @param amount 入账金额
     * @return 1 表示成功
     */
    public long credit(Long userId, BigDecimal amount) {
        return execute(creditScript, userId, amount);
    }

    /**
     * 执行出账 Lua 脚本。
     *
     * @param userId 用户 ID
     * @param amount 出账金额
     * @return 1 表示成功，-1 表示余额不足
     */
    public long debit(Long userId, BigDecimal amount) {
        return execute(debitScript, userId, amount);
    }

    /**
     * 拼接账户缓存 Key。
     *
     * @param userId 用户 ID
     * @return Redis Hash Key
     */
    public String accountKey(Long userId) {
        return "zhou6:account:" + userId;
    }

    private long execute(DefaultRedisScript<Long> script, Long userId, BigDecimal amount) {
        Long result = redisTemplate.execute(script, List.of(accountKey(userId)), money(amount));
        return result == null ? 0L : result;
    }

    private AccountSummaryVO toSummary(Map<Object, Object> values) {
        AccountSummaryVO summary = new AccountSummaryVO();
        summary.setAvailable(decimal(values.get(AVAILABLE)));
        summary.setFrozen(decimal(values.get(FROZEN)));
        summary.setSettling(decimal(values.get(SETTLING)));
        summary.setTotal(decimal(values.get(TOTAL)));
        return summary;
    }

    private BigDecimal decimal(Object value) {
        try {
            return value == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(value)).setScale(4, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new BizException(AccountErrorCode.ACCOUNT_CACHE_ERROR, "账户缓存金额格式异常", ex);
        }
    }

    private String money(BigDecimal amount) {
        BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
        return safeAmount.setScale(4, RoundingMode.UNNECESSARY).toPlainString();
    }

    private String initLockKey(Long userId) {
        return "lock:account:init:" + userId;
    }

    private String freezeLockKey(String bizType, String bizId) {
        return "lock:account:freeze:" + bizType + ":" + bizId;
    }

    private DefaultRedisScript<Long> loadScript(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setResultType(Long.class);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(path)));
        return script;
    }
}
