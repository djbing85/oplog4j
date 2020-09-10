package com.jasper.oplog.test.xml.service.impl;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jasper.oplog.test.xml.dao.UserBODao;
import com.jasper.oplog.test.xml.model.UserBO;
import com.jasper.oplog.test.xml.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserBODao dao;
    
    @Override
    public UserBO getById(Long id) {
        return dao.getById(id);
    }

    @Override
    public Long createReturnId(UserBO bo) {
        return dao.createReturnId(bo);
    }

    @Override
    public UserBO createReturnBO(UserBO bo) {
        return dao.createReturnBO(bo);
    }

    @Override
    public UserBO update(UserBO bo, String operator) {
        return dao.update(bo, operator);
    }

    @Override
    public UserBO updateIsLoaded(UserBO bo, String operator) {
        return dao.updateIsLoaded(bo, operator);
    }

    @Override
    public UserBO updateIsLoadedUserReturn(UserBO bo, String phone, Integer status) {
        return dao.updateIsLoadedUserReturn(bo, phone, status);
    }

    @Override
    public void updateNameById(Long id, String name) {
        dao.updateNameById(id, name);
    }

    @Override
    public UserBO updateBalanceById(Long id, BigDecimal balance) {
        return dao.updateBalanceById(id, balance);
    }

    @Override
    public Integer delete(Long id) {
        return dao.delete(id);
    }

    @Override
    public Integer deleteBo(UserBO bo) {
        return dao.deleteBo(bo);
    }

    @Override
    public Integer deleteBy(Long id, String by) {
        return dao.deleteBy(id, by);
    }

    @Override
    public Integer deleteFailedOpLog(Long id) {
        return dao.deleteFailedOpLog(id);
    }

}
