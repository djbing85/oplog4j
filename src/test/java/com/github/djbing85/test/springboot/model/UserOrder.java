package com.github.djbing85.test.springboot.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import com.github.djbing85.annotation.OpLogField;
import com.github.djbing85.annotation.OpLogModel;

import lombok.Data;

@Data
@OpLogModel(daoBeanId = "userOrderDao", method = "getById")
public class UserOrder {
    @OpLogField(id = 0, fieldName = "Order ID")
    private Long orderId;
    @OpLogField(fieldName = "User ID")
    private Long userId;
    @OpLogField(fieldName = "Coupon ID")
    private Long couponId;
    @OpLogField(fieldName = "Commodity ID")
    private Long commodityId;

    @OpLogField(fieldName = "Total Price", decimalFormat = "#,###.##")
    private BigDecimal totalPrice;

    @OpLogField(fieldName = "Create Time", dateFormat = "yyyy-MM-dd")
    private Date createdTime;

    @OpLogField(fieldName = "Date", dateFormat = "yyyy-MM-dd")
    private LocalDate date;
    @OpLogField(fieldName = "time", dateFormat = "HH:mm:ss")
    private LocalTime time;
    @OpLogField(fieldName = "Date Time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    @OpLogField(fieldName = "Commodity")
    private Commodity commodity;
    @OpLogField(fieldName = "Coupon")
    private Coupon coupon;
}
