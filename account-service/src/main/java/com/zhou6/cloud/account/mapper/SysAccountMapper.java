package com.zhou6.cloud.account.mapper;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.account.entity.SysAccount;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 账户快照 Mapper，提供金额与审计字段同语句更新能力。
 */
public interface SysAccountMapper extends BaseMapper<SysAccount> {

    /**
     * 将可用金额预冻结到冻结域。
     *
     * @param userId 用户 ID
     * @param amount 冻结金额
     * @param operatorId 操作人 ID
     * @return 更新行数，0 表示余额不足或账户不存在
     */
    @Update("""
            UPDATE sys_account
            SET available_amount = available_amount - #{amount},
                frozen_amount = frozen_amount + #{amount},
                update_by = #{operatorId},
                update_time = NOW(),
                version = version + 1
            WHERE user_id = #{userId}
              AND available_amount >= #{amount}
            """)
    int freezeAvailable(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    /**
     * 将冻结金额释放回可用域。
     *
     * @param userId 用户 ID
     * @param amount 解冻金额
     * @param operatorId 操作人 ID
     * @return 更新行数，0 表示冻结余额不足或账户不存在
     */
    @Update("""
            UPDATE sys_account
            SET frozen_amount = frozen_amount - #{amount},
                available_amount = available_amount + #{amount},
                update_by = #{operatorId},
                update_time = NOW(),
                version = version + 1
            WHERE user_id = #{userId}
              AND frozen_amount >= #{amount}
            """)
    int unfreeze(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    /**
     * 结算冻结金额，扣减冻结域。
     *
     * @param userId 用户 ID
     * @param amount 结算金额
     * @param operatorId 操作人 ID
     * @return 更新行数，0 表示冻结余额不足或账户不存在
     */
    @Update("""
            UPDATE sys_account
            SET frozen_amount = frozen_amount - #{amount},
                update_by = #{operatorId},
                update_time = NOW(),
                version = version + 1
            WHERE user_id = #{userId}
              AND frozen_amount >= #{amount}
            """)
    int settleFrozen(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    /**
     * 增加可用金额和历史累计金额。
     *
     * @param userId 用户 ID
     * @param amount 入账金额
     * @param operatorId 操作人 ID
     * @return 更新行数
     */
    @Update("""
            UPDATE sys_account
            SET available_amount = available_amount + #{amount},
                total_amount = total_amount + #{amount},
                update_by = #{operatorId},
                update_time = NOW(),
                version = version + 1
            WHERE user_id = #{userId}
            """)
    int creditAvailable(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    /**
     * 恢复可用金额，不增加历史累计金额。
     *
     * <p>用于红字冲正原出账流水，例如订单退款；这类资金只是退回用户可用域，
     * 不能被统计为新的历史累计获得现金。</p>
     *
     * @param userId 用户 ID
     * @param amount 恢复金额
     * @param operatorId 操作人 ID
     * @return 更新行数
     */
    @Update("""
            UPDATE sys_account
            SET available_amount = available_amount + #{amount},
                update_by = #{operatorId},
                update_time = NOW(),
                version = version + 1
            WHERE user_id = #{userId}
            """)
    int restoreAvailable(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);

    /**
     * 扣减可用金额。
     *
     * @param userId 用户 ID
     * @param amount 出账金额
     * @param operatorId 操作人 ID
     * @return 更新行数，0 表示可用余额不足或账户不存在
     */
    @Update("""
            UPDATE sys_account
            SET available_amount = available_amount - #{amount},
                update_by = #{operatorId},
                update_time = NOW(),
                version = version + 1
            WHERE user_id = #{userId}
              AND available_amount >= #{amount}
            """)
    int debitAvailable(@Param("userId") Long userId, @Param("amount") BigDecimal amount,
            @Param("operatorId") Long operatorId);
}
