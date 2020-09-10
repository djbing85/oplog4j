package com.jasper.oplog.test.xml.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.jasper.oplog.annotation.OpLogID;
import com.jasper.oplog.annotation.OpLogJoinPoint;
import com.jasper.oplog.annotation.OpLogParam;
import com.jasper.oplog.test.xml.model.SampleBO;

@Component
public class OpLogSampleDao {

    static Map<Long, SampleBO> map = new HashMap<Long, SampleBO>();
    static {
        SampleBO bo = mock();
        
        bo.setStrUserType("p");
        bo.setLongId(1L);
        bo.setStrName("jim");
        map.put(1L, bo);
        
        bo = mock();
        bo.setStrUserType("p");
        bo.setLongId(2L);
        bo.setStrName("green");
        map.put(2L, bo);
        
        bo = mock();
        bo.setStrUserType("e");
        bo.setLongId(3L);
        bo.setStrName("company A");
        map.put(3L, bo);
        
        bo = mock();
        bo.setStrUserType("e");
        bo.setLongId(4L);
        bo.setStrName("company B");
        map.put(4L, bo);
    }
    static Long maxId = 4L;
    
    public SampleBO getById(Long id) {
        return map.get(id);
    };
    
    @OpLogJoinPoint(summary = "create return Id", operator = "bo.strName")
    public Long createReturnId(SampleBO bo) {
        maxId ++;
        bo.setLongId(maxId);
        map.put(maxId, bo);
        return maxId;
    }

    @OpLogJoinPoint(summary = "create return BO", operator = "bo.strName")
    public SampleBO createReturnBO(SampleBO bo) {
        maxId ++;
        bo.setLongId(maxId);
        map.put(maxId, bo);
        return bo;
    }

    @OpLogJoinPoint(summary = "update user info", operator = "operator")
    public SampleBO update(SampleBO bo, String operator) {
        Assert.notNull(bo);
        Assert.notNull(bo.getLongId());
        map.put(bo.getLongId(), bo);
        return bo;
    }

    @OpLogJoinPoint(summary = "update user info - isLoaded: true", operator = "operator")
    public SampleBO updateIsLoaded(@OpLogParam(isLoaded = true)SampleBO bo, String operator) {
        Assert.notNull(bo);
        Assert.notNull(bo.getLongId());
        bo.setStrName(operator);
        map.put(bo.getLongId(), bo);
        return bo;
    }

    @OpLogJoinPoint(summary = "update user name", modelClass = SampleBO.class)
    public void updateNameById(@OpLogID Long id, String name) {
        Assert.notNull(id);
        Assert.notNull(name);
        SampleBO bo = map.get(id);
        Assert.notNull(bo);
        bo.setStrName(name);
        map.put(id, bo);
    }
    
    @OpLogJoinPoint(summary = "Pay Mortgage", modelClass = SampleBO.class, useReturn = true)
    public SampleBO payMortgage(@OpLogID Long id, Float pay) {
        Assert.notNull(id);
        Assert.notNull(pay);
        SampleBO bo = map.get(id);
        Assert.notNull(bo);
        bo.setDoubleBalance(bo.getDoubleBalance() - pay);
        bo.setFloatMortgage(bo.getFloatMortgage() - pay);
        //money goes to bank ... skip
        map.put(id, bo);
        return bo;
    }
    
    /**
     * @param obj could be instanceof T, or a list of T 
     */
    @OpLogJoinPoint(summary = "delete", modelClass = SampleBO.class)
    public Integer delete(@OpLogID Long id) {
        Assert.notNull(id);
        SampleBO bo = map.get(id);
        Assert.notNull(bo);
        map.remove(id);
        return 1;
    };
    
    @OpLogJoinPoint(summary = "deleteBo")
    public Integer deleteBo(SampleBO bo) {
        Assert.notNull(bo);
        SampleBO boInMap = map.get(bo.getLongId());
        Assert.notNull(boInMap);
        map.remove(boInMap.getLongId());
        return 1;
    };

    @OpLogJoinPoint(summary = "deleteBy", operator = "by", modelClass = SampleBO.class)
    public Integer deleteBy(@OpLogID Long id, String by) {
        Assert.notNull(id);
        SampleBO bo = map.get(id);
        Assert.notNull(bo);
        map.remove(id);
        return 1;
    };

    @OpLogJoinPoint(summary = "deleteFailed" )
    public Integer deleteFailed(@OpLogID Long id ) {
        Assert.notNull(id);
        SampleBO bo = map.get(id);
        Assert.notNull(bo);
        return 0;
    };
    
    public static SampleBO mock() {

        SampleBO bo = new SampleBO();
        bo.setBigDecimalLineOfCredit(new BigDecimal(987654321.7181));
        bo.setBoolIsEnable(true);
        bo.setByteField(new Byte("1"));
        bo.setCharUserType('p');
        bo.setDateCreateDate(new Date());
        bo.setDateLastLoginTime(new Date());
        bo.setDoubleBalance(10000.31415926d);
        bo.setFloatMortgage(350000.3141f);
        bo.setIgnoreField("whatever I type should be ignored");
        bo.setIntField(10086);
        bo.setShortField(new Short("1"));
        bo.setStrPsd("whoisyourdaddy");
        
        bo.setStrUserType("p");
        bo.setLongId(1L);
        bo.setStrName("jim");
        bo.setStrPhone("88888888");
        return bo;
    }
}
