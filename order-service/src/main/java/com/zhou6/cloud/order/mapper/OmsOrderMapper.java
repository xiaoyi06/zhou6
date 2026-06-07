package com.zhou6.cloud.order.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.order.entity.OmsOrder;
import org.apache.ibatis.annotations.Param;

/**
 * 订单 Mapper，所有状态推进都带期望状态条件，防止乱序流转。
 */
public interface OmsOrderMapper extends BaseMapper<OmsOrder> {

    int updateStatus(@Param("orderSn") String orderSn, @Param("expectedStatus") int expectedStatus,
            @Param("nextStatus") int nextStatus, @Param("operatorId") Long operatorId);

    List<OmsOrder> selectTimeoutOrders(@Param("status") int status, @Param("deadline") LocalDateTime deadline,
            @Param("limit") int limit);
}
