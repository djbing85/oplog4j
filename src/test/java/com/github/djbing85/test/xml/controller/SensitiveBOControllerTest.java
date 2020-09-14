package com.github.djbing85.test.xml.controller;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.djbing85.test.xml.BaseTest;
import com.github.djbing85.test.xml.dao.SensitiveBODao;
import com.github.djbing85.test.xml.model.SensitiveBO;
import com.github.djbing85.test.xml.service.SensitiveBOService;

public class SensitiveBOControllerTest extends BaseTest {
    
    @Autowired
    private SensitiveBOService service;

    @Test
    public void newBO() {
        SensitiveBO bo = SensitiveBODao.mock();
        bo = service.createReturnBO(bo);
        Assert.assertTrue(bo != null);
    }
    
    @Test
    public void newBO2() {
        SensitiveBO bo = SensitiveBODao.mock();
        Long id = service.createReturnId(bo);
        Assert.assertTrue(id != null);
    }

    @Test
    public void updateBO() {
        SensitiveBO bo = SensitiveBODao.mock();
        
        SensitiveBO bo2Update = service.getById(7L);
        bo2Update.setPhone("+861366--------");
        bo2Update.setConfidential(Integer.MAX_VALUE - 111);
        bo2Update.setSecret(Long.MAX_VALUE - 111L);
        bo2Update.setPassword("+861367++++");
        bo2Update.setRawFieldName("value");
        
        Long id = bo.getId();
        bo.setId(bo2Update.getId());
        bo2Update.setId(id);
        
        service.update(bo, "test7");
        service.update(bo2Update, "test7");
        
        service.getById(6L);
        id = bo.getId();
        bo.setId(bo2Update.getId());
        bo2Update.setId(id);
        service.update(bo, "test6");
        service.update(bo2Update, "test6");

        service.getById(5L);
        id = bo.getId();
        bo.setId(bo2Update.getId());
        bo2Update.setId(id);
        service.update(bo, "test5");
        service.update(bo2Update, "test5");

        service.getById(4L);
        id = bo.getId();
        bo.setId(bo2Update.getId());
        bo2Update.setId(id);
        service.update(bo, "test4");
        service.update(bo2Update, "test4");

        service.getById(3L);
        id = bo.getId();
        bo.setId(bo2Update.getId());
        bo2Update.setId(id);
        service.update(bo, "test3");
        service.update(bo2Update, "test3");

        service.getById(2L);
        id = bo.getId();
        bo.setId(bo2Update.getId());
        bo2Update.setId(id);
        service.update(bo, "test2");
        service.update(bo2Update, "test2");

        service.getById(1L);
        id = bo.getId();
        bo.setId(bo2Update.getId());
        bo2Update.setId(id);
        service.update(bo, "test1");
        service.update(bo2Update, "test1");

        service.getById(0L);
        id = bo.getId();
        bo.setId(bo2Update.getId());
        bo2Update.setId(id);
        service.update(bo, "test0");
        service.update(bo2Update, "test0");
    }

    @Test
    public void updatePassword() {
        Long id = 2L;
        SensitiveBO bo = service.getById(id);
        String newPswd = UUID.randomUUID().toString();
        bo.setPassword(newPswd);
        service.update(bo, "system admin");
    }

    @Test
    public void updatePhone() {
        Long id = 3L;
        SensitiveBO bo = service.getById(id);
        String newPhone = UUID.randomUUID().toString();
        bo.setPhone("new-phone-No." + newPhone);
        service.update(bo, "system admin");
    }


    @Test
    public void delete() {
        SensitiveBO bo = SensitiveBODao.mock();
        Long id = service.createReturnId(bo); 
        Integer row = service.delete(id);
        Assert.assertTrue(row == 1);
        row = service.delete2(id);
    }

    @Test
    public void deleteBo() {
        SensitiveBO bo = service.createReturnBO(SensitiveBODao.mock());
        Integer row = service.deleteBo(bo);
        Assert.assertTrue(row == 1);
    }
}
