package com.jasper.oplog.test.springboot.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jasper.oplog.test.springboot.model.Coupon;

@Component
public class CouponDao {
    
    private static Map<Long, Coupon> db = new HashMap<>();
    static {
        Coupon coupon = new Coupon();
        coupon.setId(1L);
        coupon.setName("10% OFF");
        coupon.setDiscountInt(1000);
        coupon.setPriceDouble(100D);
        db.put(1L, coupon);
        
        coupon = new Coupon();
        coupon.setId(2L);
        coupon.setName("50% OFF");
        coupon.setDiscountInt(2000);
        coupon.setPriceDouble(200D);
        db.put(2L, coupon);
    }

    public Coupon getById(Long id) {
        return db.get(id);
    }
    
    public Coupon insert(Coupon coupon) {
        db.put(coupon.getId(), coupon);
        return coupon;
    }
    
    public Boolean updateDiscount(Long id, Integer discount) {
        Coupon order = db.get(id);
        order.setDiscountInt(discount);
        db.put(id, order);
        return true;
    }
    
    public Integer deleteById(Long id) {
        Coupon coupon = db.remove(id);
        return coupon == null? 0: 1;
    }
}
