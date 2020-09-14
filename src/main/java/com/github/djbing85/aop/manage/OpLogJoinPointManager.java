/**
   Copyright 2020 Jasper J B Deng(djbing85@gmail.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.github.djbing85.aop.manage;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import com.github.djbing85.annotation.OpLogJoinPoint;
import com.github.djbing85.annotation.OpLogModel;
import com.github.djbing85.exception.OpLogException;
import com.github.djbing85.utils.OpLogUtils;

/**
 * Provide method to get model class, and to get pre-BO and/or post-BO of the join point
 * @author djbing85@gmail.com
 * @since 2019-06-11
 * @param <BO> the BUSINESS MODEL
 */
public class OpLogJoinPointManager<BO> {

    protected final Logger LOG = Logger.getLogger(this.getClass());
    
    private ApplicationContext applicationContext;
    
    /**
     * JoinPoint to build oplog
     */
    private JoinPoint joinPoint;
    
    /**
     * JoinPoint return, should be null when isPre == true<br>
     * [default] could be Void when isPre == false
     */
    private Object rvt;
    
    /**
     * isPre == true: indicated joinPoint handler processes with<code>@Before</code>
     * isPre == false: indicated joinPoint handler processes with<code>@AfterReturning</code>
     */
    private Boolean isPre;
    
    /**
     * @see OpLogJoinPointParamDescriptor
     */
    private OpLogJoinPointParamDescriptor paramProperties;
    
    public OpLogJoinPointManager(ApplicationContext applicationContext, 
            JoinPoint joinPoint, 
            Object rvt, 
            Boolean isPre) 
                    throws OpLogException {
        this.applicationContext = applicationContext;
        this.joinPoint = joinPoint;
        this.rvt = rvt;
        this.isPre = isPre;
        paramProperties = new OpLogJoinPointParamDescriptor(joinPoint, isPre);
    }

    /**
     * Get BO before or after the given JoinPoint
     * @return a BO instance
     * @throws OpLogException see e.getMessage() for more detail
     */
    @SuppressWarnings("unchecked")
    public BO getOpLogBusinessObject() throws OpLogException {
        String msg;
        MethodInvocationProceedingJoinPoint methodInvocationProceedingJoinPoint = (MethodInvocationProceedingJoinPoint) joinPoint;
        MethodSignature methodSignature = (MethodSignature)methodInvocationProceedingJoinPoint.getSignature();
        OpLogJoinPoint opLogJoinPoint = methodSignature.getMethod().getAnnotation(OpLogJoinPoint.class);

        BO obj = null;
        Class<BO> modelClass = this.getModelClass();
        
        //1. post: try to use return value for OpLogJoinPoint.useReturnValue = true
        if(!isPre) {
            Class<BO> clz = null;
            //if rvt exists
            if(rvt != null && !rvt.getClass().isInstance(Void.class)) {
                //annotation property say: use return value
                if(opLogJoinPoint.useReturn()) {
                    clz = modelClass;
                    if(clz == null) {
                        msg = "failed to get model class, please check oplog annotation config with the BO for class: " + rvt.getClass().getName() + ", method: " + methodSignature.getName();
                        LOG.error(msg);
                        throw new OpLogException(msg);
                    }
                }
            }

            if(rvt != null && clz != null && clz.isInstance(rvt)) {
                // use return value only when class matches
                if(opLogJoinPoint.useReturn()) {
                    obj = (BO)rvt;
                }
            }
            if(obj != null){
                return obj;
            }
        }

        //2: check preload param args, if no, 3. load from DAO 
        try {
            obj = (BO)OpLogUtils.loadObject(applicationContext, joinPoint, 
                    modelClass, 
                    paramProperties.getKeyArgs(), 
                    paramProperties.getIdParamMap(), 
                    paramProperties.getBoParamToLoadList(), 
                    paramProperties.getBoParamList(), 
                    isPre);
        } catch (OpLogException e) {
            LOG.error("error when loadObject: " + e.getMessage(), e);
        } catch (Throwable e) {
            LOG.error("error occur loadObject: " + e.getMessage(), e);
        }
        return obj;
    }
    
    /**
     * Get BO class in below order: <p>
     * 1. Get from method annotated by <b>OpLogJoinPoint</b>, 
     *  usually methods like <code>updateXxxById(@OpLogID Long id, String xxx)</code> should have annotation OpLogJoinPoint with property: modelClass<p>
     * 
     * 2. Get from return, methods like <code>BO insertUser(String name, String age)</code>, 
     * If OpLogJoinPoint.useReturnValue == true, will try to use BO.class <p>
     * If type mismatch, throw OpLogException<p>
     * 
     * 3. Get from param, check parameters annotated with OpLogParam, 
     *  double check annotation <b>OpLogModel</b> upon the param's class<p>
     * 
     * If no param around the method annotated by OpLogModel, throw OpLogException <p>
     * If multiple args annotated by OpLogModel, get the first one, 
     *  in this case, if user wants to generate op-log for the non-first-param, 
     *  please try to use <code>OpLogJoinPoint.modelClass</code> or <code>OpLogParam</code>
     * @return BO class
     * @throws OpLogException when found no model class, or wrong configuration
     */
    @SuppressWarnings("unchecked")
    public Class<BO> getModelClass() throws OpLogException {

        MethodInvocationProceedingJoinPoint mipjp = (MethodInvocationProceedingJoinPoint) joinPoint;
        MethodSignature methodSignature = (MethodSignature)mipjp.getSignature();
        OpLogJoinPoint aljp = methodSignature.getMethod().getAnnotation(OpLogJoinPoint.class);

        //1
        Class<BO> joinPointModelClass = null;
        if(!aljp.modelClass().getName().equals(Void.class.getName())) {
            try {
                joinPointModelClass = (Class<BO>)aljp.modelClass();
            } catch (Exception e) {
                String msg = "customized modelClass: " + aljp.modelClass().getName()
                        + " does not match expectation on method: "
                        + methodSignature.getName() + ", error: " + e.getMessage();
                LOG.error(msg, e);
                throw new OpLogException(msg);
            }
        }
        
        if(joinPointModelClass != null) {
            return joinPointModelClass;
        }
        
        //2
        Class<BO> returnClass = null;
        if(rvt != null && !rvt.getClass().isInstance(Void.class)) {
            if(aljp.useReturn()) {
                try {
                    returnClass = (Class<BO>)rvt.getClass();
                } catch (Exception e) {
                    String msg = "customized to use return value, "
                            + "but found return type class does not match expectation on method: "
                            + methodSignature.getName() + ", error: " + e.getMessage();
                    LOG.error(msg, e);
                    throw new OpLogException(msg);
                }
            }
        }
        
        if(returnClass != null) {
            return returnClass;
        }
        
        //3 it's ok to use a param as BO
        Class<BO> paramClass = null;
//        if(!CollectionUtils.isEmpty(paramProperties.getBoParamList()) ||
//                !CollectionUtils.isEmpty(paramProperties.getBoParamToLoadList())) {
//            
//            Object[] args = paramProperties.getArgs();
//            Object arg0 = args[0];
//            OpLogModel anno = arg0.getClass().getAnnotation(OpLogModel.class);
//            if(anno != null) {
//                paramClass = (Class<BO>)arg0.getClass();
//            }
//        }
        if(!CollectionUtils.isEmpty(paramProperties.getKeyArgs()) ) {
            
//            Object[] args = paramProperties.getArgs();
            Object arg0 = paramProperties.getKeyArgs().get(0);
            OpLogModel anno = arg0.getClass().getAnnotation(OpLogModel.class);
            if(anno != null) {
                paramClass = (Class<BO>)arg0.getClass();
            }
        }
        
        if(paramClass != null) {
            return paramClass;
        } else {
            String msg = "method: " + methodSignature.toLongString() + 
                    ", please config on one of below: \n" + 
                    "1. method annotation: OpLogJoinPoint.modelClass, make sure whether should have OpLogID on params; \n" + 
                    "2. if method annotation OpLogJoinPoint.useReturnValue = true, check whether return object class annotated by OpLogModel; \n"+
                    "3. param annotated by OpLogParam and param class annotated by OpLogModel;";
            LOG.error(msg);
            throw new OpLogException(msg);
        }

    }

    public JoinPoint getJoinPoint() {
        return joinPoint;
    }

    public void setJoinPoint(JoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    public Object getRvt() {
        return rvt;
    }

    public void setRvt(Object rvt) {
        this.rvt = rvt;
    }

    public Boolean getIsPre() {
        return isPre;
    }

    public void setIsPre(Boolean isPre) {
        this.isPre = isPre;
    }
}
