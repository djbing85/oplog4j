package com.jasper.oplog.test.springboot.model;

import com.jasper.oplog.annotation.OpLogField;
import com.jasper.oplog.annotation.OpLogModel;

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
