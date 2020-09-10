package com.jasper.oplog.test.springboot.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jasper.oplog.annotation.OpLogID;
import com.jasper.oplog.annotation.OpLogJoinPoint;
import com.jasper.oplog.test.springboot.dao.CommodityDao;
import com.jasper.oplog.test.springboot.model.Commodity;
import com.jasper.oplog.test.springboot.model.Coupon;

@Service
public class CommodityService {


    @Autowired
    private CommodityDao commodityDao;

    @OpLogJoinPoint(summary = "Create Commodity", useReturn = true)
    public Commodity create(Commodity o) {
        return commodityDao.insert(o);
    }

    @OpLogJoinPoint(summary = "Update Commodity Price", useReturn = true, modelClass = Coupon.class)
    public Commodity updatePrice(@OpLogID Long Id, BigDecimal price) {
        commodityDao.updatePrice(Id, price);
        return commodityDao.getById(Id);
    }

    @OpLogJoinPoint(summary = "Delete Commodity", useReturn = false, modelClass = Coupon.class)
    public Commodity delete(@OpLogID Long id) {
        Commodity o = commodityDao.getById(id);
        commodityDao.deleteById(id);
        return o;
    }

    public Commodity getById(Long id) {
        return commodityDao.getById(id);
    }
    
}
