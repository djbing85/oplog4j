package com.github.djbing85.test.xml.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.github.djbing85.annotation.OpLogID;
import com.github.djbing85.annotation.OpLogJoinPoint;
import com.github.djbing85.annotation.OpLogParam;
import com.github.djbing85.test.xml.model.UserBO;

@Component
public class UserBODao {
    
    private static Random random = new Random();
    
    private static Map<Long, UserBO> db = new HashMap<Long, UserBO>();
    
    static Long maxId = 1L;
    
    public static UserBO mock() {
        UserBO bo = new UserBO();
        bo.setBalance(new BigDecimal(random.nextLong()).add( new BigDecimal(random.nextDouble())));
        bo.setClassifyMsg("top secret: " + UUID.randomUUID());
        bo.setCreateTime(date);
        bo.setIgnoreField("invisible." + UUID.randomUUID());
        bo.setPhone("911");
        bo.setPswd("The quick brown fox jumps over the lazy dog");
        bo.setStatus(random.nextDouble() >= 0.5D? 1: 0);
        bo.setAuditStatus(random.nextDouble() >= 0.5D? 1: 0);
        bo.setType(random.nextDouble() >= 0.5D? "p" : "e");
        bo.setUserId(maxId);
        bo.setUserName("jasper.d." + UUID.randomUUID());
        maxId ++;
        return bo;
    }
    
    static Date date = new Date();
    static {
        UserBO bo = mock();
        db.put(bo.getUserId(), bo);
        bo = mock();
        db.put(bo.getUserId(), bo);
        bo = mock();
        db.put(bo.getUserId(), bo);
        bo = mock();
        db.put(bo.getUserId(), bo);
    }
    
    public UserBO getById(Long id) {
        UserBO user = db.get(id);
        if(user == null) {
            return null;
        }
        //return a copy of the BO
        UserBO bo = new UserBO();
        BeanUtils.copyProperties(user, bo);
        return bo;
    };
    
    @OpLogJoinPoint(summary = "create user", operator = "bo.userName")
    public Long createReturnId(UserBO bo) {
        maxId ++;
        bo.setUserId(maxId);
        db.put(maxId, bo);
        return maxId;
    }

    @OpLogJoinPoint(summary = "create return BO", operator = "bo.userName")
    public UserBO createReturnBO(UserBO bo) {
        maxId ++;
        bo.setUserId(maxId);
        db.put(maxId, bo);
        return bo;
    }

    @OpLogJoinPoint(summary = "update user info", operator = "operator")
    public UserBO update(UserBO bo, String operator) {
        Assert.notNull(bo);
        Assert.notNull(bo.getUserId());
        db.put(bo.getUserId(), bo);
        return bo;
    }

    @OpLogJoinPoint(summary = "update user info - isLoaded: true", operator = "operator")
    public UserBO updateIsLoaded(@OpLogParam(isLoaded = true)UserBO bo, String operator) {
        Assert.notNull(bo);
        Assert.notNull(bo.getUserId());
        db.put(bo.getUserId(), bo);
        return bo;
    }

    @OpLogJoinPoint(summary = "update user info - isLoaded&userReturn", useReturn = true)
    public UserBO updateIsLoadedUserReturn(@OpLogParam(isLoaded = true)UserBO bo, String phone, Integer status) {
        Assert.notNull(bo);
        Assert.notNull(bo.getUserId());
        bo.setStatus(status);
        bo.setPhone(phone);
        db.put(bo.getUserId(), bo);
        return bo;
    }

    @OpLogJoinPoint(summary = "update user name", modelClass = UserBO.class)
    public void updateNameById(@OpLogID Long id, String name) {
        Assert.notNull(id);
        UserBO bo = db.get(id);
        Assert.notNull(bo);
        bo.setUserName(name);
        db.put(id, bo);
    }

    @OpLogJoinPoint(summary = "update user balance", useReturn=true)
    public UserBO updateBalanceById(@OpLogID Long id, BigDecimal balance) {
        Assert.notNull(id);
        UserBO bo = db.get(id);
        Assert.notNull(bo);
        bo.setBalance(balance);
        db.put(id, bo);
        return bo;
    }
    
    @OpLogJoinPoint(summary = "delete by ID", modelClass = UserBO.class)
    public Integer delete(@OpLogID Long id) {
        Assert.notNull(id);
        UserBO bo = db.get(id);
        Assert.notNull(bo);
        db.remove(id);
        return 1;
    };
    
    @OpLogJoinPoint(summary = "delete by BO")
    public Integer deleteBo(UserBO bo) {
        Assert.notNull(bo);
        UserBO boInMap = db.get(bo.getUserId());
        Assert.notNull(boInMap);
        db.remove(boInMap.getUserId());
        return 1;
    };

    @OpLogJoinPoint(summary = "delete by ID specify operator", operator = "by", modelClass = UserBO.class)
    public Integer deleteBy(@OpLogID Long id, String by) {
        Assert.notNull(id);
        UserBO bo = db.get(id);
        Assert.notNull(bo);
        db.remove(id);
        return 1;
    };

    @OpLogJoinPoint(summary = "delete failed: no modelClass" )
    public Integer deleteFailedOpLog(@OpLogID Long id ) {
        Assert.notNull(id);
        UserBO bo = db.get(id);
        Assert.notNull(bo);
        return 0;
    };
}
