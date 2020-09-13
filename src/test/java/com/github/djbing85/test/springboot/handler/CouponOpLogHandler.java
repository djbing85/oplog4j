package com.github.djbing85.test.springboot.handler;

import com.github.djbing85.aop.handler.IOpLogHandler;
import com.github.djbing85.model.DefaultOpLog;
import com.github.djbing85.test.springboot.model.Coupon;

public class CouponOpLogHandler implements IOpLogHandler<Coupon> {

    @Override
    public Class<Coupon> getModelClass() {
        return Coupon.class;
    }

    @Override
    public void handleDiff(DefaultOpLog<Coupon> log) {
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
