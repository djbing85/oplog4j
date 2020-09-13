package com.github.djbing85.test.springboot.handler;

import com.alibaba.fastjson.JSONObject;
import com.github.djbing85.aop.handler.IOpLogHandler;
import com.github.djbing85.model.DefaultOpLog;
import com.github.djbing85.model.DiffModel;
import com.github.djbing85.test.springboot.model.OrderChange;


public class OrderChangeOpLogHandler implements IOpLogHandler<OrderChange> {

    @Override
    public Class<OrderChange> getModelClass() {
        return OrderChange.class;
    }

    @Override
    public void handleDiff(DefaultOpLog<OrderChange> log) {
        System.out.println("summary: " + log.getSummary());
        System.out.println("operator: " + log.getOperator());
        System.out.println("pre: " + log.getPre());
        System.out.println("post: " + log.getPost());
//        System.out.println("diff: " + log.getDiff());
        System.out.println("diff: " + JSONObject.toJSONString(log.getDiff()));
        System.out.println("opTime: " + log.getOpTime());
        System.out.println("opType: " + log.getOpType());
        System.out.println("modelClass: " + log.getModelClass());
        
        DiffModel diff = null;
        Object obj = log.getDiff();
        if(obj instanceof DiffModel) {
            diff = (DiffModel) obj;
            while(diff != null) {
                System.out.println(diff.getFieldName() +": "+ diff.getFrom() +" ---> "+ diff.getTo());
            }
            
        }
        
    }

}
