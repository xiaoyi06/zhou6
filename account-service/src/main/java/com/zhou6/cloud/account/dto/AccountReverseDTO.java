package com.zhou6.cloud.account.dto;

/**
 * 红字冲正请求参数。
 */
public class AccountReverseDTO {

    /** 原始流水业务类型。 */
    private String origBizType;
    /** 原始流水业务唯一号。 */
    private String origBizId;
    /** 冲正流水业务类型。 */
    private String reverseBizType;
    /** 冲正流水业务唯一号。 */
    private String reverseBizId;
    /** 执行冲正的管理员 ID。 */
    private Long adminId;

    public String getOrigBizType() {
        return origBizType;
    }

    public void setOrigBizType(String origBizType) {
        this.origBizType = origBizType;
    }

    public String getOrigBizId() {
        return origBizId;
    }

    public void setOrigBizId(String origBizId) {
        this.origBizId = origBizId;
    }

    public String getReverseBizType() {
        return reverseBizType;
    }

    public void setReverseBizType(String reverseBizType) {
        this.reverseBizType = reverseBizType;
    }

    public String getReverseBizId() {
        return reverseBizId;
    }

    public void setReverseBizId(String reverseBizId) {
        this.reverseBizId = reverseBizId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }
}
