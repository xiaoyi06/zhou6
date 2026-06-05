package com.zhou6.cloud.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.order.entity.OmsOrderPayReceipt;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单现金扣减凭证 Mapper。
 */
public interface OmsOrderPayReceiptMapper extends BaseMapper<OmsOrderPayReceipt> {

    @Update("""
            UPDATE oms_order_pay_receipt
            SET receipt_status = #{nextStatus},
                update_by = #{operatorId},
                update_time = NOW()
            WHERE order_sn = #{orderSn}
              AND receipt_status = #{expectedStatus}
            """)
    int updateStatus(@Param("orderSn") String orderSn, @Param("expectedStatus") int expectedStatus,
            @Param("nextStatus") int nextStatus, @Param("operatorId") Long operatorId);
}
