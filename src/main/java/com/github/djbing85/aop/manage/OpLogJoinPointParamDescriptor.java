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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

import com.github.djbing85.annotation.OpLogID;
import com.github.djbing85.annotation.OpLogModel;
import com.github.djbing85.annotation.OpLogParam;
import com.github.djbing85.constants.OpLogConstants;
import com.github.djbing85.exception.OpLogException;

/**
 * parse joinPoint parameter properties
 * @author djbing85@gmail.com
 * @since 2019-06-11
 */
public class OpLogJoinPointParamDescriptor implements Serializable {
    
    /**  */
    private static final long serialVersionUID = -8823637218196527703L;

    protected final Logger LOG = Logger.getLogger(this.getClass());
    
    private JoinPoint joinPoint;
    
    /** param annotated with OpLogID, param type should be Integer/Long/String, other types are not supported */
    private Map<Integer, Map<String, Object>> idParamMap;
    //parameter type annotated with OpLogModel: pending to load from outer storage
    private List<Parameter> boParamToLoadList;
    //params annotated with OpLogParam: no need to load from outer storage
    private List<Parameter> boParamList;
    /** param object */
    private List<Object> keyArgs;
    
    /**
     * parse the joinPoint parameters
     * @param joinPoint the join point
     * @param isPre true means is intercepted by @Before, false: @After
     * @throws OpLogException see e.getMessage() for more detail
     */
    public OpLogJoinPointParamDescriptor(JoinPoint joinPoint, boolean isPre) throws OpLogException {
        this.joinPoint = joinPoint;
        
        MethodInvocationProceedingJoinPoint mipjp = (MethodInvocationProceedingJoinPoint) joinPoint;
        MethodSignature methodSignature = (MethodSignature)mipjp.getSignature();

        Method method = methodSignature.getMethod();
        Parameter[] parameters = method.getParameters();

        idParamMap = new HashMap<Integer, Map<String, Object>>();
        boParamToLoadList = new ArrayList<Parameter>();
        boParamList = new ArrayList<Parameter>();
        keyArgs = new ArrayList<Object>();

        Annotation[] paramAnnotations;
        String msg;
        if(parameters != null) {
            Object [] args = joinPoint.getArgs();
            Integer paramCount = parameters.length;
            Parameter param;
            OpLogParam opLogParamAnno;
            OpLogID opLogID;
            for(int i = 0; i < paramCount; i++) {
                param = parameters[i];
                paramAnnotations = param.getAnnotations();
                if(paramAnnotations != null) {
                    for(Annotation anno: paramAnnotations) {
                        if(anno instanceof OpLogID) {
                            opLogID = (OpLogID) anno;
                            if(args[i] instanceof Integer ||
                                    args[i] instanceof Long ||
                                    args[i] instanceof String) {
                                if(idParamMap.containsKey(opLogID.order())) {
                                    msg = "method: " + method.getName() + ", OpLogID found duplidated order: " + opLogID.order();
                                    LOG.error(msg);
                                    throw new OpLogException(msg);
                                }
                                Map<String, Object> idMap = new HashMap<String, Object>();
                                idMap.put(OpLogConstants.PARAM, param);
                                idMap.put(OpLogConstants.ARG, args[i]);
                                idParamMap.put(opLogID.order(), idMap);
                            }
                            else {
                                msg = "method: " + method.getName() + ", OpLogID should only apply to param in type: Integer|Long|String";
                                LOG.error(msg);
                                throw new OpLogException(msg);
                            }
                        } else if(anno instanceof OpLogParam) {
                            opLogParamAnno = (OpLogParam) anno;
                            if(args[i].getClass().getAnnotation(OpLogModel.class) != null) {
                                if(isPre && opLogParamAnno.isLoaded()) {
                                    boParamList.add(param);
                                    keyArgs.add(args[i]);
                                } else {
                                    if(boParamToLoadList.size() == 0) {
                                        boParamToLoadList.add(param);
                                        keyArgs.add(args[i]);
                                    } else {
                                        msg = "method: " + method.getName() + " only one param annotated with OpLogParam is allowed";
                                        LOG.error(msg);
                                        throw new OpLogException(msg);
                                    }
                                }
                            } else {
                                msg = "method: " + method.getName() + ", param: " + param.getName() + ", param class: " + args[i].getClass().getName() + " annotation OpLogModel not found";
                                LOG.error(msg);
                                throw new OpLogException(msg);
                            }
                        }
                    }
                }
                //implicit annotation OpLogModel upon the class of the args, 
                //may have multiple target, but try to get the first one only
                Annotation modelAnno = args[i].getClass().getAnnotation(OpLogModel.class);
                if(modelAnno != null) {
                    if(boParamToLoadList.size() == 0) {
                        boParamToLoadList.add(param);
                        keyArgs.add(args[i]);
                    }
                }
            }
        }
    }
    
    public Object[] getArgs() {
        return joinPoint.getArgs();
    }
    

    public JoinPoint getJoinPoint() {
        return joinPoint;
    }

    public Map<Integer, Map<String, Object>> getIdParamMap() {
        return idParamMap;
    }

    public List<Parameter> getBoParamToLoadList() {
        return boParamToLoadList;
    }

    public List<Parameter> getBoParamList() {
        return boParamList;
    }

    public List<Object> getKeyArgs() {
        return keyArgs;
    }

}
