package com.github.djbing85.test.springboot.model;

import com.github.djbing85.annotation.OpLogField;
import com.github.djbing85.annotation.OpLogModel;

import lombok.Data;

@Data
@OpLogModel(daoBeanId = "orderChangeService", method = "orderDetail")
public class OrderChange {
    
    @OpLogField(id = 0, fieldName = "Order ID")
    private Long orderId;

    @OpLogField(fieldName = "Order")
    private UserOrder order;
    @OpLogField(fieldName = "Commodity")
    private Commodity commodity;
    @OpLogField(fieldName = "Coupon")
    private Coupon coupon;
}
