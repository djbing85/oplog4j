package com.jasper.oplog.test.springboot.handler;

import com.alibaba.fastjson.JSONObject;
import com.jasper.oplog.aop.handler.IOpLogHandler;
import com.jasper.oplog.model.DefaultOpLog;
import com.jasper.oplog.test.springboot.model.UserOrder;

public class OrderOpLogHandler implements IOpLogHandler<UserOrder> {

    @Override
    public Class<UserOrder> getModelClass() {
        return UserOrder.class;
    }

    @Override
    public void handleDiff(DefaultOpLog<UserOrder> log) {
        System.out.println("summary: " + log.getSummary());
        System.out.println("operator: " + log.getOperator());
        System.out.println("pre: " + log.getPre());
        System.out.println("post: " + log.getPost());
        System.out.println("diff: " + JSONObject.toJSONString(log.getDiff()));
        System.out.println("opTime: " + log.getOpTime());
        System.out.println("opType: " + log.getOpType());
        System.out.println("modelClass: " + log.getModelClass());
    }

}
