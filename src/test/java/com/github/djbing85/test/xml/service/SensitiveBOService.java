package com.github.djbing85.test.xml.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.djbing85.annotation.OpLogID;
import com.github.djbing85.annotation.OpLogJoinPoint;
import com.github.djbing85.test.xml.dao.SensitiveBODao;
import com.github.djbing85.test.xml.model.SensitiveBO;

@Service
public class SensitiveBOService {
    
    @Autowired
    private SensitiveBODao dao;

    
    public SensitiveBO getById(Long id) {
        return dao.getById(id);
    }

    @OpLogJoinPoint(summary = "create", operator = "bo.phone")
    public Long createReturnId(SensitiveBO bo) {
        return dao.createReturnId(bo);
    }

    @OpLogJoinPoint(summary = "create return BO", operator = "bo")
    public SensitiveBO createReturnBO(SensitiveBO bo) {
        return dao.createReturnBO(bo);
    }

    @OpLogJoinPoint(summary = "update BO info", operator = "operator")
    public SensitiveBO update(SensitiveBO bo, String operator) {
        return dao.update(bo, operator);
    }

    @OpLogJoinPoint(summary = "delete by ID", modelClass = SensitiveBO.class)
    public Integer delete(@OpLogID Long id) {
        return dao.delete(id);
    }

    @OpLogJoinPoint(summary = "delete by ID but no @OpLogID", modelClass = SensitiveBO.class)
    public Integer delete2(Long id) {
        return dao.delete(id);
    }

    @OpLogJoinPoint(summary = "delete by BO")
    public Integer deleteBo(SensitiveBO bo) {
        return dao.deleteBo(bo);
    }

}
