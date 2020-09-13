package com.github.djbing85.test.springboot.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.github.djbing85.test.springboot.model.UserOrder;

@Component
public class UserOrderDao {
    
    private static Map<Long, UserOrder> db = new HashMap<>();
    static {
        UserOrder order = new UserOrder();
        order.setCommodityId(1L);
        order.setCouponId(1L);
        order.setCreatedTime(new Date());
        order.setDate(LocalDate.now());
        order.setDateTime(LocalDateTime.now());
        order.setTime(LocalTime.now());
        order.setOrderId(1L);
        order.setTotalPrice(new BigDecimal(100));
        order.setUserId(1L);
        db.put(1L, order);
        
        order = new UserOrder();
        order.setCommodityId(2L);
        order.setCouponId(2L);
        order.setCreatedTime(new Date());
        order.setDate(LocalDate.now());
        order.setDateTime(LocalDateTime.now());
        order.setTime(LocalTime.now());
        order.setOrderId(2L);
        order.setTotalPrice(new BigDecimal(200));
        order.setUserId(2L);
        db.put(2L, order);
    }

    public UserOrder getById(Long id) {
        return db.get(id);
    }
    
    public UserOrder insert(UserOrder order) {
        order.setCreatedTime(new Date());
        order.setDate(LocalDate.now());
        order.setDateTime(LocalDateTime.now());
        order.setTime(LocalTime.now());
        db.put(order.getOrderId(), order);
        return order;
    }
    
    public Boolean updateTotalPrice(Long orderId, BigDecimal totalPrice) {
        UserOrder order = db.get(orderId);
        order.setTotalPrice(totalPrice);
        db.put(orderId, order);
        return true;
    }
    
    public Integer deleteById(Long orderId) {
        UserOrder order = db.remove(orderId);
        return order == null? 0: 1;
    }
}
