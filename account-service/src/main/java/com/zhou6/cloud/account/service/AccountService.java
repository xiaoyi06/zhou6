package com.zhou6.cloud.account.service;

import java.math.BigDecimal;

import com.zhou6.cloud.account.vo.AccountSummaryVO;

/**
 * 账户业务服务接口，定义现金账户查询、冻结、结算、解冻、入账、出账和冲正能力。
 */
public interface AccountService {

    /**
     * 查询首页现金看板，缓存未命中时自动开户并回填 Redis。
     *
     * @param userId 用户 ID
     * @return 现金汇总
     */
    AccountSummaryVO summary(Long userId);

    /**
     * 预冻结可用余额。
     *
     * @param userId 用户 ID
     * @param amount 冻结金额
     * @param bizType 业务类型
     * @param bizId 业务唯一号
     * @return true 表示冻结成功
     */
    boolean freeze(Long userId, BigDecimal amount, String bizType, String bizId);

    /**
     * 结算冻结金额，插入流水并扣减冻结域。
     *
     * @param userId 用户 ID
     * @param amount 结算金额
     * @param bizType 业务类型
     * @param bizId 业务唯一号
     * @param operatorId 操作人 ID
     * @param remark 备注
     */
    void settleFrozen(Long userId, BigDecimal amount, String bizType, String bizId, Long operatorId, String remark);

    /**
     * 解冻现金，将冻结金额释放回可用余额。
     *
     * @param userId 用户 ID
     * @param amount 解冻金额
     * @param bizType 业务类型
     * @param bizId 业务唯一号
     * @param operatorId 操作人 ID
     * @param remark 备注
     */
    void unfreeze(Long userId, BigDecimal amount, String bizType, String bizId, Long operatorId, String remark);

    /**
     * 现金入账。
     *
     * @param userId 用户 ID
     * @param amount 入账金额
     * @param bizType 业务类型
     * @param bizId 业务唯一号
     * @param operatorId 操作人 ID
     * @param remark 备注
     */
    void credit(Long userId, BigDecimal amount, String bizType, String bizId, Long operatorId, String remark);

    /**
     * 现金出账。
     *
     * @param userId 用户 ID
     * @param amount 出账金额
     * @param bizType 业务类型
     * @param bizId 业务唯一号
     * @param operatorId 操作人 ID
     * @param remark 备注
     */
    void debit(Long userId, BigDecimal amount, String bizType, String bizId, Long operatorId, String remark);

    /**
     * 红字冲正，按原流水方向生成相反方向的新流水。
     *
     * @param origBizType 原始流水业务类型
     * @param origBizId 原始流水业务唯一号
     * @param reverseBizType 冲正流水业务类型
     * @param reverseBizId 冲正流水业务唯一号
     * @param adminId 管理员 ID
     */
    void reverse(String origBizType, String origBizId, String reverseBizType, String reverseBizId, Long adminId);
}
