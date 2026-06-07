package com.zhou6.cloud.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhou6.cloud.order.entity.OmsOrderPayReceipt;
import org.apache.ibatis.annotations.Param;

/**
 * 订单现金扣减凭证 Mapper。
 */
public interface OmsOrderPayReceiptMapper extends BaseMapper<OmsOrderPayReceipt> {

    int updateStatus(@Param("orderSn") String orderSn, @Param("expectedStatus") int expectedStatus,
            @Param("nextStatus") int nextStatus, @Param("operatorId") Long operatorId);
}
