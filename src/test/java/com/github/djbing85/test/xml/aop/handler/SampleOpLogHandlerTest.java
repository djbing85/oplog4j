package com.github.djbing85.test.xml.aop.handler;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.djbing85.test.xml.BaseTest;
import com.github.djbing85.test.xml.dao.OpLogSampleDao;
import com.github.djbing85.test.xml.model.SampleBO;

public class SampleOpLogHandlerTest extends BaseTest {

    @Autowired
    private OpLogSampleDao opLogSampleDao;

//    @Autowired
//    private SampleOpLogHandler sampleOpLogHandler;
    
    @Test
    public void payMortgage() {
        Long id = 4L;
        Float pay = 40f;
        SampleBO rowPre = opLogSampleDao.getById(id);
        Float floatMortgage = rowPre.getFloatMortgage();
        Double balance = rowPre.getDoubleBalance();
        
        SampleBO rowPost = opLogSampleDao.payMortgage(id, pay);
        Assert.assertTrue(rowPre !=null && rowPost != null );
        Assert.assertTrue(floatMortgage - pay - rowPost.getFloatMortgage() == 0d );
        Assert.assertTrue(balance - pay - rowPost.getDoubleBalance() == 0d );
    }
    
    @Test
    public void update() {
        SampleBO bo = OpLogSampleDao.mock();
        bo.setLongId(4L);
        bo.setStrName("test user 2");
        bo.setBigDecimalLineOfCredit(new BigDecimal(123789.0));
        bo.setBoolIsEnable(false);
        bo.setByteField((byte)30);
        bo.setCharUserType('e');
        bo.setDateLastLoginTime(new Date());
        bo.setDoubleBalance(789789D);

        bo.setStrPhone("12388888");
        
        SampleBO row = opLogSampleDao.update(bo, "test user 3");
        Assert.assertTrue(row != null && row.getLongId().equals(new Long(4L)));

        bo = OpLogSampleDao.mock();
        bo.setLongId(2L);
        bo.setFloatMortgage(2345984560329F);
        bo.setIgnoreField("invisible");
        bo.setIntField(2000000143);
        bo.setShortField((short) 256);
        bo.setStrName("anthony");
        bo.setStrPsd("psd123");
        bo.setStrUserType("p");
        row = opLogSampleDao.update(bo, "test user 4");
        Assert.assertTrue(row != null && row.getLongId().equals(new Long(2L)));
    }

    @Test
    public void updateNameById() {
        Long id = 4L;
        String newName = "a new name";
        opLogSampleDao.updateNameById(id, newName);
        SampleBO row = opLogSampleDao.getById(id);
        Assert.assertTrue(row != null && row.getLongId().equals(id));
        Assert.assertTrue(row.getStrName().equals(newName));
    }


    @Test
    public void updateIsLoaded() {
        SampleBO bo = OpLogSampleDao.mock();
        String newName = "a new name2";
        SampleBO row = opLogSampleDao.updateIsLoaded(bo, newName);
        Assert.assertTrue(row != null && row.getLongId().equals(bo.getLongId()));
        Assert.assertTrue(row.getStrName().equals(newName));
    }

    @Test
    public void createReturnId() {
        SampleBO bo = OpLogSampleDao.mock();
        bo.setLongId(null);
        Long id = opLogSampleDao.createReturnId(bo);
        SampleBO row = opLogSampleDao.getById(id);
        Assert.assertTrue(row != null);
        Assert.assertTrue(row.getLongId().equals(id));
        
    }
    
    @Test
    public void createReturnBO() {
        SampleBO bo = OpLogSampleDao.mock();
        bo.setLongId(null);
        SampleBO row = opLogSampleDao.createReturnBO(bo);
        Assert.assertTrue(row != null);
        Assert.assertTrue(row.getLongId() != null);
    }

    @Test
    public void delete() {
        Long id = 1L;
        Integer row = null;
//        row = opLogSampleDao.delete(id);
//        Assert.assertTrue(row == 1);
//        
//        id = 2L;
//        SampleBO bo = opLogSampleDao.getById(id);
//        row = opLogSampleDao.deleteBo(bo);
//        Assert.assertTrue(row == 1);
        
        id = 3L;
        row = opLogSampleDao.deleteBy(id, "tester A");
        Assert.assertTrue(row == 1);
        
        id = 4L;
        row = opLogSampleDao.deleteFailed(id);
        Assert.assertTrue(row == 0);
    }
}
