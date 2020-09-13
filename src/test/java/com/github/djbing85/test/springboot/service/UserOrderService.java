package com.github.djbing85.test.springboot.service;

import java.math.BigDecimal;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.djbing85.annotation.OpLogID;
import com.github.djbing85.annotation.OpLogJoinPoint;
import com.github.djbing85.test.springboot.dao.CommodityDao;
import com.github.djbing85.test.springboot.dao.CouponDao;
import com.github.djbing85.test.springboot.dao.UserOrderDao;
import com.github.djbing85.test.springboot.model.Commodity;
import com.github.djbing85.test.springboot.model.Coupon;
import com.github.djbing85.test.springboot.model.OrderChange;
import com.github.djbing85.test.springboot.model.UserOrder;

@Service
public class UserOrderService {

    @Autowired
    private UserOrderDao orderDao;

    @Autowired
    private CommodityDao commodityDao;

    @Autowired
    private CouponDao couponDao;
    
    public UserOrder getById(Long orderId) {
        return orderDao.getById(orderId);
    }

    @OpLogJoinPoint(summary = "Create Order", useReturn = true)
    public UserOrder createOrder(UserOrder order) {
        return orderDao.insert(order);
    }

    @OpLogJoinPoint(summary = "Update Order's Total Price", useReturn = true, modelClass = UserOrder.class)
    public UserOrder updateTotalPrice(@OpLogID Long orderId, BigDecimal totalPrice) {
        orderDao.updateTotalPrice(orderId, totalPrice);
        return orderDao.getById(orderId);
    }

    @OpLogJoinPoint(summary = "Cancel Order", useReturn = false, modelClass = UserOrder.class)
    public UserOrder cancelOrder(@OpLogID Long orderId) {
        UserOrder o = orderDao.getById(orderId);
        orderDao.deleteById(orderId);
        return o;
    }
    
}
