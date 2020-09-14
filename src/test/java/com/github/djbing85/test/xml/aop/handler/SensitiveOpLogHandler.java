package com.github.djbing85.test.xml.aop.handler;

import com.github.djbing85.aop.handler.IOpLogHandler;
import com.github.djbing85.model.DefaultOpLog;
import com.github.djbing85.test.xml.model.SensitiveBO;

/**
 * test cases upon UserDAO
 * @author djbing85@gmail.com
 * @since 2019-06-14
 */
public class SensitiveOpLogHandler implements IOpLogHandler<SensitiveBO> {

    @Override 
    public Class<SensitiveBO> getModelClass() {
        return SensitiveBO.class;
    }

    @Override
    public void handleDiff(DefaultOpLog<SensitiveBO> log) {
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