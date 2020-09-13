package com.github.djbing85.test.springboot.handler;

import com.github.djbing85.aop.handler.IOpLogHandler;
import com.github.djbing85.model.DefaultOpLog;
import com.github.djbing85.test.springboot.model.Commodity;

public class CommodityOpLogHandler implements IOpLogHandler<Commodity> {

    @Override
    public Class<Commodity> getModelClass() {
        return Commodity.class;
    }

    @Override
    public void handleDiff(DefaultOpLog<Commodity> log) {
        System.out.println("summary: " + log.getSummary());
        System.out.println("operator: " + log.getOperator());
        System.out.println("pre: " + log.getPre());
        System.out.println("post: " + log.getPost());
        System.out.println("diff: " + log.getDiff());
        System.out.println("opTime: " + log.getOpTime());
        System.out.println("opType: " + log.getOpType());
        System.out.println("modelClass: " + log.getModelClass());
    }

}
