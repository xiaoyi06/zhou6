package com.zhou6.cloud.order.mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.order.entity.OmsOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 订单 Mapper，所有状态推进都带期望状态条件，防止乱序流转。
 */
public interface OmsOrderMapper extends BaseMapper<OmsOrder> {

    @Update("""
            UPDATE oms_order
            SET order_status = #{nextStatus},
                update_by = #{operatorId},
                update_time = NOW()
            WHERE order_sn = #{orderSn}
              AND order_status = #{expectedStatus}
            """)
    int updateStatus(@Param("orderSn") String orderSn, @Param("expectedStatus") int expectedStatus,
            @Param("nextStatus") int nextStatus, @Param("operatorId") Long operatorId);

    @Select("""
            SELECT id, order_sn, user_id, total_amount, pay_amount, order_status,
                   create_by, create_time, update_by, update_time
            FROM oms_order
            WHERE order_status = #{status}
              AND create_time <= #{deadline}
            ORDER BY create_time ASC
            LIMIT #{limit}
            """)
    List<OmsOrder> selectTimeoutOrders(@Param("status") int status, @Param("deadline") LocalDateTime deadline,
            @Param("limit") int limit);
}
