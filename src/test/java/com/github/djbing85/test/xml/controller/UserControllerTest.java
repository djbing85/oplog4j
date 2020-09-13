package com.github.djbing85.test.xml.controller;

import java.util.Random;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.djbing85.test.xml.BaseTest;
import com.github.djbing85.test.xml.dao.UserBODao;
import com.github.djbing85.test.xml.model.UserBO;
import com.github.djbing85.test.xml.service.UserService;

public class UserControllerTest extends BaseTest {
    
    @Autowired
    private UserService service;

    @Test
    public void newUser() {
        UserBO user = UserBODao.mock();
        Long userId = service.createReturnId(user);
        Assert.assertTrue(userId != null);
    }

    @Test
    public void updateUser() {
        UserBO user = UserBODao.mock();
        user.setUserId(1L);
        service.update(user, "system admin");
    }

    @Test
    public void updatePassword() {
        Long userId = 2L;
        UserBO user = service.getById(userId);
        String newPswd = UUID.randomUUID().toString();
        user.setPswd(newPswd);
        service.update(user, "system admin");
    }

    @Test
    public void updatePhone() {
        Long userId = 3L;
        UserBO user = service.getById(userId);
        String newPhone = UUID.randomUUID().toString();
        user.setPhone("new-phone-No." + newPhone);
        service.update(user, "system admin");
    }

    @Test
    public void auditUser() {
        Long userId = 4L;
        UserBO user = service.getById(userId);
        Random r = new Random();
        Integer auditStatus;
        Double randomDouble = r.nextDouble();
        if(randomDouble >= 0.67D) {
            auditStatus = 3;
        } else if(randomDouble >= 0.33D) {
            auditStatus = 2;
        } else {
            auditStatus = 1;
        }
        user.setAuditStatus(auditStatus);
        service.update(user, "auditor jasper.d");
    }

    @Test
    public void delete() {
        UserBO user = UserBODao.mock();
        Long userId = service.createReturnId(user);
        Integer row = service.delete(userId);
        Assert.assertTrue(row == 1);
    }

    @Test
    public void deleteBo() {
        UserBO user = service.createReturnBO(UserBODao.mock());
        Integer row = service.deleteBo(user);
        Assert.assertTrue(row == 1);
    }

    @Test
    public void deleteBy() {
        UserBO user = UserBODao.mock();
        Long userId = service.createReturnId(user);
        Integer row = service.deleteBy(userId, "jas.d");
        Assert.assertTrue(row == 1);
    }

    @Test
    public void deleteFailedOpLog() {
        UserBO user = UserBODao.mock();
        Long userId = service.createReturnId(user);
        Integer row = service.deleteFailedOpLog(userId);
        Assert.assertTrue(row == 0);
    }
    
}
