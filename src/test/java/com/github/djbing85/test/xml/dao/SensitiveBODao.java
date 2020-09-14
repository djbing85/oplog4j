package com.github.djbing85.test.xml.dao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.github.djbing85.test.xml.model.SensitiveBO;

@Component
public class SensitiveBODao {
    
    private static Random random = new Random();
    
    private static Map<Long, SensitiveBO> db = new HashMap<Long, SensitiveBO>();
    
    static Long maxId = 1L;
    
    public static SensitiveBO mock() {
        SensitiveBO bo = new SensitiveBO();
        String str = "";
        bo.setPhone(str);
        bo.setConfidential(0);
        bo.setSecret(0L);
        bo.setPassword(str);
        bo.setId(maxId);
        maxId ++;
        return bo;
    }
    
    static Date date = new Date();
    static {
        SensitiveBO bo = mock();
        db.put(bo.getId(), bo);
        
        bo = mock();
        bo.setPhone("+");
        bo.setConfidential(1);
        bo.setSecret(1L);
        bo.setPassword("+");
        db.put(bo.getId(), bo);
        
        bo = mock();
        bo.setPhone("+8");
        bo.setConfidential(12);
        bo.setSecret(12L);
        bo.setPassword("+8");
        db.put(bo.getId(), bo);
        
        bo = mock();
        bo.setPhone("+86");
        bo.setConfidential(123);
        bo.setSecret(123L);
        bo.setPassword("+86");
        
        bo = mock();
        bo.setPhone("+861");
        bo.setConfidential(1234);
        bo.setSecret(1234L);
        bo.setPassword("+861");
        db.put(bo.getId(), bo);
        
        bo = mock();
        bo.setPhone("+8613");
        bo.setConfidential(12345);
        bo.setSecret(12345L);
        bo.setPassword("+8613");
        db.put(bo.getId(), bo);
        
        bo = mock();
        bo.setPhone("+86136");
        bo.setConfidential(123456);
        bo.setSecret(123456L);
        bo.setPassword("+86136");
        db.put(bo.getId(), bo);
        
        bo = mock();
        bo.setPhone("+861366");
        bo.setConfidential(1234567);
        bo.setSecret(1234567L);
        bo.setPassword("+861367");
        db.put(bo.getId(), bo);

        bo = mock();
        bo.setPhone("+8613660000000");
        bo.setConfidential(Integer.MAX_VALUE);
        bo.setSecret(Long.MAX_VALUE);
        bo.setPassword("+861367");
        db.put(bo.getId(), bo);
    }
    
    public SensitiveBO getById(Long id) {
        SensitiveBO user = db.get(id);
        if(user == null) {
            return null;
        }
        //return a copy of the BO
        SensitiveBO bo = new SensitiveBO();
        BeanUtils.copyProperties(user, bo);
        return bo;
    };
    
    public Long createReturnId(SensitiveBO bo) {
        maxId ++;
        bo.setId(maxId);
        db.put(maxId, bo);
        return maxId;
    }

    public SensitiveBO createReturnBO(SensitiveBO bo) {
        maxId ++;
        bo.setId(maxId);
        db.put(maxId, bo);
        return bo;
    }

    public SensitiveBO update(SensitiveBO bo, String operator) {
        Assert.notNull(bo);
        Assert.notNull(bo.getId());
        db.put(bo.getId(), bo);
        return bo;
    }
    
    public Integer delete(Long id) {
        Assert.notNull(id);
        SensitiveBO bo = db.get(id);
        if(bo != null) {
            db.remove(id);
            return 1;
        }
        return 0;
    };
    
    public Integer deleteBo(SensitiveBO bo) {
        Assert.notNull(bo);
        SensitiveBO boInMap = db.get(bo.getId());
        Assert.notNull(boInMap);
        db.remove(boInMap.getId());
        return 1;
    };

}
