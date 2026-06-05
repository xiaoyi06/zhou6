package com.zhou6.cloud.account.controller;

import com.zhou6.cloud.account.dto.AccountAmountDTO;
import com.zhou6.cloud.account.dto.AccountReverseDTO;
import com.zhou6.cloud.account.dto.AccountUserDTO;
import com.zhou6.cloud.account.handler.AccountErrorCode;
import com.zhou6.cloud.account.service.AccountService;
import com.zhou6.cloud.account.vo.AccountSummaryVO;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "现金账户", description = "提供现金账户汇总查询和原子变更接口")
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 查询用户首页现金看板。
     *
     * @param dto 用户 ID 参数
     * @return 可用、冻结、待结算和累计现金汇总
     */
    @PostMapping("/summary")
    @Operation(summary = "查询首页现金看板", description = "按用户ID查询可用、冻结、待结算和累计现金")
    public R<AccountSummaryVO> summary(@RequestBody AccountUserDTO dto) {
        if (dto == null) {
            throw new BizException(AccountErrorCode.BIZ_PARAM_INVALID);
        }
        return R.ok(accountService.summary(dto.getUserId()));
    }

    /**
     * 预冻结现金余额，供订单等同步业务调用。
     *
     * @param dto 冻结金额和业务唯一号
     * @return true 表示冻结成功
     */
    @PostMapping("/freeze")
    @Operation(summary = "现金预冻结", description = "从 Redis 中原子扣减可用余额并增加冻结余额")
    public R<Boolean> freeze(@RequestBody AccountAmountDTO dto) {
        requireAmountRequest(dto);
        return R.ok(accountService.freeze(dto.getUserId(), dto.getAmount(), dto.getBizType(), dto.getBizId()));
    }

    /**
     * 现金入账接口，生成入账流水并更新账户快照。
     *
     * @param dto 入账金额、业务唯一号和操作人
     * @return 空响应
     */
    @PostMapping("/credit")
    @Operation(summary = "现金入账", description = "新增入账流水并更新账户快照")
    public R<Void> credit(@RequestBody AccountAmountDTO dto) {
        requireAmountRequest(dto);
        accountService.credit(dto.getUserId(), dto.getAmount(), dto.getBizType(), dto.getBizId(),
                dto.getOperatorId(), dto.getRemark());
        return R.ok(null);
    }

    /**
     * 现金出账接口，生成出账流水并扣减可用余额。
     *
     * @param dto 出账金额、业务唯一号和操作人
     * @return 空响应
     */
    @PostMapping("/debit")
    @Operation(summary = "现金出账", description = "新增出账流水并扣减可用余额")
    public R<Void> debit(@RequestBody AccountAmountDTO dto) {
        requireAmountRequest(dto);
        accountService.debit(dto.getUserId(), dto.getAmount(), dto.getBizType(), dto.getBizId(),
                dto.getOperatorId(), dto.getRemark());
        return R.ok(null);
    }

    /**
     * 红字冲正接口，按原始流水生成方向相反的新流水。
     *
     * @param dto 原流水业务号、冲正业务号和管理员 ID
     * @return 空响应
     */
    @PostMapping("/reverse")
    @Operation(summary = "红字冲正", description = "按原始流水生成方向相反的新流水，不修改原流水")
    public R<Void> reverse(@RequestBody AccountReverseDTO dto) {
        if (dto == null) {
            throw new BizException(AccountErrorCode.BIZ_PARAM_INVALID);
        }
        accountService.reverse(dto.getOrigBizType(), dto.getOrigBizId(), dto.getReverseBizType(),
                dto.getReverseBizId(), dto.getAdminId());
        return R.ok(null);
    }

    private void requireAmountRequest(AccountAmountDTO dto) {
        if (dto == null) {
            throw new BizException(AccountErrorCode.BIZ_PARAM_INVALID);
        }
    }
}
