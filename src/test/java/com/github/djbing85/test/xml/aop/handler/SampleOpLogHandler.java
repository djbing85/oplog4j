package com.github.djbing85.test.xml.aop.handler;

import com.github.djbing85.aop.handler.IOpLogHandler;
import com.github.djbing85.model.DefaultOpLog;
import com.github.djbing85.test.xml.model.SampleBO;

/**
 * @author djbing85@gmail.com
 * @since 2019-05-10
 */
public class SampleOpLogHandler implements IOpLogHandler<SampleBO> {

    @Override
    public Class<SampleBO> getModelClass() {
        return SampleBO.class;
    }

    @Override
    public void handleDiff(DefaultOpLog<SampleBO> log) {
        System.out.println("summary: " + log.getSummary());
        System.out.println("operator: " + log.getOperator());
        System.out.println("pre: " + log.getPre());
        System.out.println("post: " + log.getPost());
        System.out.println("diff: " + log.getDiff());
        
    }

}