package com.jasper.oplog.test.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jasper.oplog.annotation.OpLogID;
import com.jasper.oplog.annotation.OpLogJoinPoint;
import com.jasper.oplog.test.springboot.dao.CouponDao;
import com.jasper.oplog.test.springboot.model.Coupon;

@Service
public class CouponService {


    @Autowired
    private CouponDao couponDao;

    @OpLogJoinPoint(summary = "Create Coupon", useReturn = true)
    public Coupon create(Coupon o) {
        return couponDao.insert(o);
    }

    @OpLogJoinPoint(summary = "Update Coupon Discount", useReturn = true, modelClass = Coupon.class)
    public Coupon updateDiscount(@OpLogID Long id, Integer discount) {
        couponDao.updateDiscount(id, discount);
        return couponDao.getById(id);
    }

    @OpLogJoinPoint(summary = "Delete Coupon", useReturn = false, modelClass = Coupon.class)
    public Coupon delete(@OpLogID Long id) {
        Coupon o = couponDao.getById(id);
        couponDao.deleteById(id);
        return o;
    }

    public Coupon getById(Long id) {
        return couponDao.getById(id);
    }
    
}
