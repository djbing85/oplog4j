package com.github.djbing85.test.springboot.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.djbing85.annotation.OpLogJoinPoint;
import com.github.djbing85.test.springboot.dao.CommodityDao;
import com.github.djbing85.test.springboot.dao.CouponDao;
import com.github.djbing85.test.springboot.dao.UserOrderDao;
import com.github.djbing85.test.springboot.model.Commodity;
import com.github.djbing85.test.springboot.model.Coupon;
import com.github.djbing85.test.springboot.model.OrderChange;
import com.github.djbing85.test.springboot.model.UserOrder;

@Service
public class OrderChangeService {

    @Autowired
    private UserOrderDao orderDao;

    @Autowired
    private CommodityDao commodityDao;

    @Autowired
    private CouponDao couponDao;

    @OpLogJoinPoint(summary = "Order Change", useReturn = true)
    public OrderChange orderChange(OrderChange change) {
        orderDao.updateTotalPrice(change.getOrderId(), change.getOrder().getTotalPrice());
        commodityDao.updatePrice(change.getCommodity().getId(), change.getCommodity().getPriceBigDecimal());
        couponDao.updateDiscount(change.getCoupon().getId(), change.getCoupon().getDiscountInt());
        return orderDetail(change.getOrderId());
    }

    public OrderChange orderDetail(Long orderId) {
        UserOrder order = orderDao.getById(orderId);
        Coupon coupon = couponDao.getById(order.getCouponId());
        Commodity commodity = commodityDao.getById(order.getCommodityId());
        OrderChange change = new OrderChange();
        change.setOrderId(orderId);
        UserOrder order2 = new UserOrder();
        BeanUtils.copyProperties(order, order2);
        Coupon coupon2 = new Coupon();
        BeanUtils.copyProperties(coupon, coupon2);
        Commodity commodity2 = new Commodity();
        BeanUtils.copyProperties(commodity, commodity2);
        
        change.setOrder(order2);
        change.setCoupon(coupon2);
        change.setCommodity(commodity2);
        return change;
    }
    
}
