package com.github.djbing85.test.springboot.controller;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.github.djbing85.model.DiffModel;
import com.github.djbing85.test.springboot.OpLog4jApplication;
import com.github.djbing85.test.springboot.model.OrderChange;
import com.github.djbing85.test.springboot.model.UserOrder;
import com.github.djbing85.test.springboot.service.CommodityService;
import com.github.djbing85.test.springboot.service.CouponService;
import com.github.djbing85.test.springboot.service.OrderChangeService;
import com.github.djbing85.test.springboot.service.UserOrderService;
import com.github.djbing85.test.springboot.utils.OpLog4jMessageUtils;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = { OpLog4jApplication.class }, 
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Controller
public class UserOrderControllerTest {

    @Autowired
    private UserOrderService orderService;

    @Autowired
    private OrderChangeService orderChangeService;

    @Autowired
    private CouponService couponService;
    
    @Autowired
    private CommodityService commodityService;

    @Test
    public void createOrder() {
        UserOrder order = orderService.getById(2L);
        order.setTotalPrice(new BigDecimal(500));
        order.setOrderId(3L);
        UserOrder o = orderService.createOrder(order);
        Assert.assertTrue(o != null && o.getOrderId().equals(3L));
    }

    @Test
    public void updateTotalPrice() {
        Long orderId = 2L;
        BigDecimal totalPrice = new BigDecimal(333);
        UserOrder o = orderService.updateTotalPrice(orderId, totalPrice);
        Assert.assertTrue(o != null && o.getTotalPrice().equals(totalPrice));
    }

    @Test
    public void cancelOrder() {
        Long orderId = 1L;
        UserOrder o = orderService.cancelOrder(orderId);
        Assert.assertTrue(o != null);
    }

    @Test
    public void orderChange() {
        OrderChange change = orderChangeService.orderDetail(2L);
        change.getOrder().setTotalPrice(new BigDecimal(1111));
        change.getCommodity().setPriceBigDecimal(new BigDecimal(543));
        change.getCoupon().setDiscountInt(667);
        orderChangeService.orderChange(change);
    }
    

    @RequestMapping(value = "/i18nTest", 
        method = RequestMethod.GET, produces = {"application/json"})
    @ResponseBody
    public String i18nTest() {
        String diff = "{\"fieldName\":\"Coupon\",\"subModelDiffList\":{\"fieldName\":\"coupon.discount\",\"from\":\"\\\"20.00%\\\"\",\"to\":\"\\\"6.67%\\\"\"}}\r\n";
        DiffModel dm = JSONObject.parseObject(diff, DiffModel.class);
        OpLog4jMessageUtils.i18nFieldName(dm);
        return JSONObject.toJSONString(dm);
    }
}
