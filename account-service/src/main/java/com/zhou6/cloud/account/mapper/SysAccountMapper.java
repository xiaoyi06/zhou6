package com.zhou6.cloud.account.mapper;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.account.entity.SysAccount;
import org.apache.ibatis.annotations.Param;

/**
 * 账户快照 Mapper，提供金额与审计字段同语句更新能力。
 */
public interface SysAccountMapper extends BaseMapper<SysAccount> {

    int freezeAvailable(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    int unfreeze(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    int settleFrozen(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    int creditAvailable(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    int restoreAvailable(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    int debitAvailable(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);
}
