package com.jasper.oplog.test.xml.service;

import java.math.BigDecimal;

import com.jasper.oplog.test.xml.model.UserBO;

public interface UserService {

    UserBO getById(Long id);

    Long createReturnId(UserBO bo);

    UserBO createReturnBO(UserBO bo);

    UserBO update(UserBO bo, String operator);

    UserBO updateIsLoaded(UserBO bo, String operator);

    UserBO updateIsLoadedUserReturn(UserBO bo, String phone, Integer status);

    void updateNameById(Long id, String name);

    UserBO updateBalanceById(Long id, BigDecimal balance);

    Integer delete(Long id);

    Integer deleteBo(UserBO bo);

    Integer deleteBy(Long id, String by);

    Integer deleteFailedOpLog(Long id);
}
