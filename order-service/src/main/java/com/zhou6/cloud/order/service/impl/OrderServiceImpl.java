package com.zhou6.cloud.order.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhou6.cloud.common.context.UserContextHolder;
import com.zhou6.cloud.common.dto.R;
import com.zhou6.cloud.common.handler.BizException;
import com.zhou6.cloud.order.client.AccountClient;
import com.zhou6.cloud.order.dto.AccountAmountDTO;
import com.zhou6.cloud.order.dto.AccountReverseDTO;
import com.zhou6.cloud.order.dto.OrderCreateDTO;
import com.zhou6.cloud.order.entity.OmsOrder;
import com.zhou6.cloud.order.entity.OmsOrderPayReceipt;
import com.zhou6.cloud.order.enums.OrderStatus;
import com.zhou6.cloud.order.enums.ReceiptStatus;
import com.zhou6.cloud.order.handler.OrderErrorCode;
import com.zhou6.cloud.order.mapper.OmsOrderMapper;
import com.zhou6.cloud.order.mapper.OmsOrderPayReceiptMapper;
import com.zhou6.cloud.order.mq.AccountMessage;
import com.zhou6.cloud.order.mq.AccountMessagePublisher;
import com.zhou6.cloud.order.service.OrderService;
import com.zhou6.cloud.order.vo.OrderVO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单服务实现，负责订单状态机、账户预冻结和事务后置 MQ。
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final String BIZ_TYPE_ORDER_PAY = "ORDER_PAY";
    private static final String BIZ_TYPE_ORDER_REFUND = "ORDER_REFUND";
    private static final int MONEY_SCALE = 4;
    private static final int MONEY_PRECISION = 16;
    private static final DateTimeFormatter ORDER_SN_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OmsOrderMapper orderMapper;
    private final OmsOrderPayReceiptMapper receiptMapper;
    private final AccountClient accountClient;
    private final AccountMessagePublisher accountMessagePublisher;

    public OrderServiceImpl(OmsOrderMapper orderMapper, OmsOrderPayReceiptMapper receiptMapper,
            AccountClient accountClient, AccountMessagePublisher accountMessagePublisher) {
        this.orderMapper = orderMapper;
        this.receiptMapper = receiptMapper;
        this.accountClient = accountClient;
        this.accountMessagePublisher = accountMessagePublisher;
    }

    /**
     * 创建订单并立即发起支付。
     *
     * <p>本方法先插入待支付订单，再同步调用账户服务预冻结现金；本地事务提交后发送结算 MQ，
     * 如果本地事务在预冻结后回滚，则发送解冻补偿 MQ。</p>
     *
     * @param dto 订单金额参数
     * @return 支付后的订单详情
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createAndPay(OrderCreateDTO dto) {
        Long userId = requireCurrentUserId();
        requireCreateRequest(dto);
        BigDecimal totalAmount = requireAmount(dto.getTotalAmount());
        BigDecimal payAmount = requireAmount(dto.getPayAmount());
        if (payAmount.compareTo(totalAmount) > 0) {
            throw new BizException(OrderErrorCode.PARAM_INVALID, "实付金额不能大于订单总金额");
        }
        String orderSn = newOrderSn();
        OmsOrder order = buildOrder(orderSn, userId, totalAmount, payAmount);
        try {
            orderMapper.insert(order);
        } catch (DuplicateKeyException ex) {
            throw new BizException(OrderErrorCode.ORDER_SN_DUPLICATED, OrderErrorCode.ORDER_SN_DUPLICATED.getMessage(), ex);
        }
        freezeAccount(userId, payAmount, orderSn);
        accountMessagePublisher.sendUnfreezeAfterRollback(
                buildAccountMessage(userId, payAmount, orderSn, userId, "订单本地事务回滚后释放预冻结现金"));
        receiptMapper.insert(buildReceipt(orderSn, userId, payAmount));
        updateOrderStatus(orderSn, OrderStatus.WAIT_PAY, OrderStatus.PAID, userId);
        accountMessagePublisher.sendSettleAfterCommit(buildAccountMessage(userId, payAmount, orderSn, userId, "订单支付成功结算"));
        return toVO(findByOrderSn(orderSn));
    }

    /**
     * 按订单号查询订单详情。
     *
     * @param orderSn 订单唯一业务单号
     * @return 订单详情
     */
    @Override
    public OrderVO detail(String orderSn) {
        return toVO(requireOrder(orderSn));
    }

    /**
     * 取消超时仍处于待支付状态的订单，并在事务提交后发送解冻消息。
     *
     * @param orderSn 订单唯一业务单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrder(String orderSn) {
        OmsOrder order = requireOrder(orderSn);
        if (order.getOrderStatus() == null || order.getOrderStatus() != OrderStatus.WAIT_PAY.getCode()) {
            return;
        }
        int orderUpdated = orderMapper.updateStatus(orderSn, OrderStatus.WAIT_PAY.getCode(),
                OrderStatus.CANCELED.getCode(), 0L);
        if (orderUpdated == 0) {
            return;
        }
        int receiptUpdated = receiptMapper.updateStatus(orderSn, ReceiptStatus.FROZEN.getCode(),
                ReceiptStatus.RELEASED.getCode(), 0L);
        if (receiptUpdated > 0) {
            accountMessagePublisher.sendUnfreezeAfterCommit(
                    buildAccountMessage(order.getUserId(), order.getPayAmount(), orderSn, 0L, "订单超时自动取消解冻"));
        }
    }

    /**
     * 审批退款，将订单从已支付推进到退款中，再调用账户服务冲正，最后推进到已退款。
     *
     * @param orderSn 订单唯一业务单号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(String orderSn) {
        Long adminId = requireCurrentUserId();
        OmsOrder order = requireOrder(orderSn);
        if (order.getOrderStatus() == null || order.getOrderStatus() != OrderStatus.PAID.getCode()) {
            throw new BizException(OrderErrorCode.ORDER_STATUS_INVALID);
        }
        updateOrderStatus(orderSn, OrderStatus.PAID, OrderStatus.REFUNDING, adminId);
        reverseAccount(orderSn, adminId);
        updateOrderStatus(orderSn, OrderStatus.REFUNDING, OrderStatus.REFUNDED, adminId);
    }

    /**
     * 同步调用账户服务完成现金预冻结。
     *
     * @param userId 下单用户 ID
     * @param amount 预冻结金额
     * @param orderSn 订单唯一业务单号
     */
    private void freezeAccount(Long userId, BigDecimal amount, String orderSn) {
        AccountAmountDTO request = new AccountAmountDTO();
        request.setUserId(userId);
        request.setAmount(amount);
        request.setBizType(BIZ_TYPE_ORDER_PAY);
        request.setBizId(orderSn);
        request.setOperatorId(userId);
        request.setRemark("订单现金预冻结");
        R<Boolean> response;
        try {
            response = accountClient.freeze(request);
        } catch (RuntimeException ex) {
            throw new BizException(OrderErrorCode.ACCOUNT_FREEZE_FAILED, "账户预冻结调用失败", ex);
        }
        if (response == null || !response.success() || !Boolean.TRUE.equals(response.getData())) {
            throw new BizException(OrderErrorCode.ACCOUNT_FREEZE_FAILED,
                    response == null ? OrderErrorCode.ACCOUNT_FREEZE_FAILED.getMessage() : response.getMessage());
        }
    }

    /**
     * 同步调用账户服务完成退款红字冲正。
     *
     * @param orderSn 订单唯一业务单号
     * @param adminId 退款审批管理员 ID
     */
    private void reverseAccount(String orderSn, Long adminId) {
        AccountReverseDTO request = new AccountReverseDTO();
        request.setOrigBizType(BIZ_TYPE_ORDER_PAY);
        request.setOrigBizId(orderSn);
        request.setReverseBizType(BIZ_TYPE_ORDER_REFUND);
        request.setReverseBizId(orderSn);
        request.setAdminId(adminId);
        R<Void> response;
        try {
            response = accountClient.reverse(request);
        } catch (RuntimeException ex) {
            throw new BizException(OrderErrorCode.ACCOUNT_REVERSE_FAILED, "账户冲正调用失败", ex);
        }
        if (response == null || !response.success()) {
            throw new BizException(OrderErrorCode.ACCOUNT_REVERSE_FAILED,
                    response == null ? OrderErrorCode.ACCOUNT_REVERSE_FAILED.getMessage() : response.getMessage());
        }
    }

    /**
     * 构造订单主表实体，初始状态固定为待支付。
     *
     * @param orderSn 订单唯一业务单号
     * @param userId 下单用户 ID
     * @param totalAmount 订单原始总金额
     * @param payAmount 实付现金金额
     * @return 订单实体
     */
    private OmsOrder buildOrder(String orderSn, Long userId, BigDecimal totalAmount, BigDecimal payAmount) {
        LocalDateTime now = LocalDateTime.now();
        OmsOrder order = new OmsOrder();
        order.setOrderSn(orderSn);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setOrderStatus(OrderStatus.WAIT_PAY.getCode());
        order.setCreateBy(userId);
        order.setCreateTime(now);
        order.setUpdateBy(userId);
        order.setUpdateTime(now);
        return order;
    }

    /**
     * 构造订单现金扣减凭证，记录账户系统已预冻结金额。
     *
     * @param orderSn 订单唯一业务单号
     * @param userId 下单用户 ID
     * @param payAmount 已预冻结金额
     * @return 订单现金扣减凭证实体
     */
    private OmsOrderPayReceipt buildReceipt(String orderSn, Long userId, BigDecimal payAmount) {
        LocalDateTime now = LocalDateTime.now();
        OmsOrderPayReceipt receipt = new OmsOrderPayReceipt();
        receipt.setOrderSn(orderSn);
        receipt.setUserId(userId);
        receipt.setFreezeAmount(payAmount);
        receipt.setReceiptStatus(ReceiptStatus.FROZEN.getCode());
        receipt.setCreateBy(userId);
        receipt.setCreateTime(now);
        receipt.setUpdateBy(userId);
        receipt.setUpdateTime(now);
        return receipt;
    }

    /**
     * 构造发送到账户服务的标准 MQ 消息。
     *
     * @param userId 账户所属用户 ID
     * @param amount 账户变更金额
     * @param orderSn 订单唯一业务单号
     * @param operatorId 操作人 ID
     * @param remark 消息备注
     * @return 账户 MQ 消息
     */
    private AccountMessage buildAccountMessage(Long userId, BigDecimal amount, String orderSn, Long operatorId,
            String remark) {
        AccountMessage message = new AccountMessage();
        message.setUserId(userId);
        message.setAmount(amount);
        message.setBizType(BIZ_TYPE_ORDER_PAY);
        message.setBizId(orderSn);
        message.setOperatorId(operatorId);
        message.setRemark(remark);
        return message;
    }

    /**
     * 按期望状态推进订单状态，防止状态乱序流转。
     *
     * @param orderSn 订单唯一业务单号
     * @param expected 当前期望状态
     * @param next 目标状态
     * @param operatorId 操作人 ID
     */
    private void updateOrderStatus(String orderSn, OrderStatus expected, OrderStatus next, Long operatorId) {
        int updated = orderMapper.updateStatus(orderSn, expected.getCode(), next.getCode(), operatorId);
        if (updated == 0) {
            throw new BizException(OrderErrorCode.ORDER_STATUS_INVALID);
        }
    }

    /**
     * 查询订单并要求订单存在。
     *
     * @param orderSn 订单唯一业务单号
     * @return 订单实体
     */
    private OmsOrder requireOrder(String orderSn) {
        requireOrderSn(orderSn);
        OmsOrder order = findByOrderSn(orderSn);
        if (order == null) {
            throw new BizException(OrderErrorCode.ORDER_NOT_FOUND);
        }
        return order;
    }

    /**
     * 按订单号查询订单。
     *
     * @param orderSn 订单唯一业务单号
     * @return 订单实体，不存在时返回 null
     */
    private OmsOrder findByOrderSn(String orderSn) {
        return orderMapper.selectOne(new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getOrderSn, orderSn));
    }

    /**
     * 获取当前登录用户 ID，订单链路不允许匿名操作。
     *
     * @return 当前登录用户 ID
     */
    private Long requireCurrentUserId() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null || userId <= 0) {
            throw new BizException(OrderErrorCode.PARAM_INVALID, "未获取到当前登录用户");
        }
        return userId;
    }

    /**
     * 校验创建订单请求体。
     *
     * @param dto 创建订单请求体
     */
    private void requireCreateRequest(OrderCreateDTO dto) {
        if (dto == null) {
            throw new BizException(OrderErrorCode.PARAM_INVALID);
        }
    }

    /**
     * 校验订单号格式。
     *
     * @param orderSn 订单唯一业务单号
     */
    private void requireOrderSn(String orderSn) {
        if (orderSn == null || orderSn.isBlank() || orderSn.length() > 64) {
            throw new BizException(OrderErrorCode.PARAM_INVALID);
        }
    }

    /**
     * 校验金额必须大于 0，且最多 4 位小数、总精度不超过 16 位。
     *
     * @param amount 原始金额
     * @return 标准化后的金额
     */
    private BigDecimal requireAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(OrderErrorCode.AMOUNT_INVALID);
        }
        BigDecimal normalized;
        try {
            normalized = amount.setScale(MONEY_SCALE, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new BizException(OrderErrorCode.AMOUNT_INVALID, "金额最多支持4位小数", ex);
        }
        if (normalized.precision() > MONEY_PRECISION) {
            throw new BizException(OrderErrorCode.AMOUNT_INVALID, "金额整数和小数总位数不能超过16位");
        }
        return normalized;
    }

    /**
     * 生成订单业务单号。
     *
     * @return 订单唯一业务单号
     */
    private String newOrderSn() {
        int suffix = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return "O" + ORDER_SN_TIME_FORMAT.format(LocalDateTime.now()) + suffix;
    }

    /**
     * 将订单实体转换为接口响应对象。
     *
     * @param order 订单实体
     * @return 订单响应对象
     */
    private OrderVO toVO(OmsOrder order) {
        OrderVO vo = new OrderVO();
        vo.setOrderSn(order.getOrderSn());
        vo.setUserId(order.getUserId() == null ? null : String.valueOf(order.getUserId()));
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }
}
