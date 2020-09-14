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
package com.github.djbing85.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.github.djbing85.annotation.OpLogField;
import com.github.djbing85.annotation.OpLogModel;
import com.github.djbing85.constants.OpLogConstants;
import com.github.djbing85.exception.OpLogException;

/**
 * @author djbing85@gmail.com
 * @since 2019-05-27
 */
public class OpLogUtils {
    
    protected static final Logger LOG = Logger.getLogger(OpLogUtils.class);

    /**
     * To compose an operation difference, we need PRE and POST Business Object(BO) <br/>
     * This method gets the PRE and POST value from DAO, 
     *  if user specified, we will also use param BO directly 
     * @param applicationContext spring applicationContext
     * @param modelClass BO model class
     * @param keyArgs id param(s) or model argument
     * @param idParamMap id Parameter
     * @param boParamToLoadList Business Object Parameter, to load from DAO
     * @param boParamList Business Object Parameter, in which, annotated with <Code>OpLogParam(isLoaded = true)</code>
     * @return latest values of the Business Object 
     * @throws OpLogException
     */
    public synchronized static Object loadObject(
            ApplicationContext applicationContext, 
            JoinPoint jp, 
            @SuppressWarnings("rawtypes") Class modelClass, 
            List<Object> keyArgs, 
            Map<Integer, Map<String, Object>> idParamMap,
            List<Parameter> boParamToLoadList, 
            List<Parameter> boParamList, Boolean isPre) 
                    throws OpLogException {
        
        validateParams(applicationContext, jp, modelClass, keyArgs, idParamMap, boParamToLoadList, boParamList);
        
        String msg;
        
        @SuppressWarnings("unchecked")
        Annotation anno = modelClass.getAnnotation(OpLogModel.class);
        OpLogModel opLogModelAnno = (OpLogModel)anno;
        String daoBeanId = opLogModelAnno.daoBeanId();
        String getByIdMethod = opLogModelAnno.method();
        Object dao = applicationContext.getBean(daoBeanId);
        
        if(idParamMap != null && idParamMap.size() > 0) {
            @SuppressWarnings("rawtypes")
            Class[] parameterTypes = new Class[idParamMap.size()];
            Object[] args = new Object[idParamMap.size()];
            for(int i = 0; i < idParamMap.size(); i++) {
                Parameter p = (Parameter)idParamMap.get(i).get(OpLogConstants.PARAM);
                parameterTypes[i] = p.getType();
                Object arg = idParamMap.get(i).get(OpLogConstants.ARG);
                args[i] = arg;
            }

            try {
                Method method = dao.getClass().getMethod(getByIdMethod, parameterTypes);
                return method.invoke(dao, args);
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new OpLogException(e.getMessage(), e);
            }
        }
        if(boParamList != null) {
            if(isPre && boParamList.size() > 0 && keyArgs.size() > 0) {
                return keyArgs.get(0);
            }
        }
        if(boParamToLoadList != null && boParamToLoadList.size() > 0) {
            if(boParamToLoadList.size() == 1) {
                Field[] fields = modelClass.getDeclaredFields();
                Map<Integer, Map<String, Object>> idFieldMap = new HashMap<Integer, Map<String, Object>>();
                Map<String, Object> idMap;
                for(Field f: fields) {
                    OpLogField opLogFieldAnno = f.getDeclaredAnnotation(OpLogField.class);
                    if(opLogFieldAnno != null) {
                        if(opLogFieldAnno.id() >=0 ) {
                            if(idFieldMap.containsKey(opLogFieldAnno.id()) ) {
                                msg = "duplicate OpLogField.id value found on class: " + modelClass.getName();
                                LOG.error(msg);
                                throw new OpLogException(msg);
                            }
                            Object value = invokeGet(keyArgs.get(0), f.getName());
                            //value could be null in create function, and as well DAO should get null later
                            idMap = new HashMap<String, Object>();
                            idMap.put(OpLogConstants.FIELD_VAL, value);
                            idMap.put(OpLogConstants.FIELD_TYPE, f.getType());
                            idFieldMap.put(opLogFieldAnno.id(), idMap);
                        }
                    }
                }

                Set<Integer> keySet = idFieldMap.keySet();

                Class<?>[] parameterTypes = new Class[idFieldMap.size()];
                Object[] keyValues = new Object[idFieldMap.size()];

                //params in order
                for(Integer idOrder: keySet) {
                    idMap = idFieldMap.get(idOrder);
                    parameterTypes[idOrder] = (Class<?>)idMap.get(OpLogConstants.FIELD_TYPE);
                    keyValues[idOrder] = idMap.get(OpLogConstants.FIELD_VAL);
                }

                try {
                    Method method = dao.getClass().getMethod(getByIdMethod, parameterTypes);
                    return method.invoke(dao, keyValues);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException 
                        | IllegalArgumentException | InvocationTargetException e) {
                    throw new OpLogException("fail to execute method: " + getByIdMethod + ", " + e.getMessage(), e);
                }
            }
        }
        
        throw new OpLogException("please check your configuration to load model from DAO/Service.");
    }
    
    private static void validateParams(ApplicationContext applicationContext,
            JoinPoint jp, 
            @SuppressWarnings("rawtypes") Class modelClass, 
            List<Object> keyArgs, 
            Map<Integer, Map<String, Object>> idParamMap,
            List<Parameter> boParamToLoadList, 
            List<Parameter> boParamList)
                    throws OpLogException {

        String msg;
        if(idParamMap != null) {
            if(idParamMap.size() > 0) {
                if(boParamToLoadList != null && boParamToLoadList.size() > 0) {
                    msg = "OpLogID or OpLogModel should be apply only one: " + jp.toLongString();
                    LOG.error(msg);
                    throw new OpLogException(msg);
                }
                if(boParamList != null && boParamList.size() > 0) {
                    msg = "OpLogID or OpLogModel should be apply only one: " + jp.toLongString();
                    LOG.error(msg);
                    throw new OpLogException(msg);
                }
                if(modelClass.equals(Void.class)) {
                    msg = "please specify OpLogJoinPoint.modelClass upon: " + jp.toLongString();
                    LOG.error(msg);
                    throw new OpLogException(msg);
                }
                
                Integer idParamSize = idParamMap.size();
                Set<Integer> idOrderSet = idParamMap.keySet();
                for(Integer order: idOrderSet) {
                    if(order + 1 > idParamSize) {
                        msg = "OpLogID.order [" + order + "] is invalid, please check on method: " + jp.toLongString();
                        LOG.error(msg);
                        throw new OpLogException(msg);
                    }
                }
            }
        }
        if(boParamToLoadList != null) {
            if(boParamToLoadList.size() > 1) {
                msg = "should have but only one parameter annotated by OpLogParam: " + jp.toLongString();
                LOG.error(msg);
                throw new OpLogException(msg);
            }
        }
        if(boParamList != null) {
            if(boParamList.size() > 1) {
                msg = "should have but only one parameter annotated with OpLogParam: " + jp.toLongString();
                LOG.error(msg);
                throw new OpLogException(msg);
            }
        }
        if(CollectionUtils.isEmpty(keyArgs) && CollectionUtils.isEmpty(boParamList)) {
            msg = "please check around method: " + jp.getSignature().toLongString() + 
                    " whether 1: specify an OpLogID and modelClass or, 2: using OpLogModel but the ID field is null";
            LOG.error(msg);
            throw new OpLogException(msg);
        }
        
        //a BO model must be annotated by OpLogModel correctly, otherwise there is no way to load the model by DAO 
        @SuppressWarnings("unchecked")
        Annotation anno = modelClass.getAnnotation(OpLogModel.class);
        if(anno == null) {
            msg = "target model class should be annotated with OpLogModel. method: " + jp.toLongString();
            LOG.error(msg);
            throw new OpLogException(msg);
        }
        
        OpLogModel OpLogModelAnno = (OpLogModel)anno;
        String daoBeanId = OpLogModelAnno.daoBeanId();
        if(StringUtils.isEmpty(daoBeanId)) {
            msg = "daoBeanId not found from OpLogModel around method: " + jp.toLongString();
            LOG.error(msg);
            throw new OpLogException(msg);
        }
        Object dao = applicationContext.getBean(daoBeanId);
        
        if(dao == null) {
            msg = "daoBeanId: " + daoBeanId + " bean not found around method: " + jp.toLongString();
            LOG.error(msg);
            throw new OpLogException(msg);
        }
    }
    
    public synchronized static Object invokeGet(Object o, String fieldName) throws OpLogException {
        try {
            Method method = makeGetMethod(o.getClass(), fieldName);
            return method.invoke(o, new Object[0]);
        } catch (Exception e) {
            String msg = "error when invokeGet for objectClass: " + o.getClass() 
            + ", fieldName: " + fieldName;
            LOG.error(msg, e);
            throw new OpLogException(msg);
        }
    }

    
    @SuppressWarnings("unchecked")
    private static Method makeGetMethod(@SuppressWarnings("rawtypes") Class objectClass, String fieldName) {
        StringBuffer sb = new StringBuffer();
        sb.append("get");
        sb.append(fieldName.substring(0, 1).toUpperCase());
        sb.append(fieldName.substring(1));
        try {
            return objectClass.getMethod(sb.toString());
        } catch (Exception e) {
            LOG.error("error when makeGetMethod for objectClass: " + objectClass 
                    + ", fieldName: " + fieldName, e);
        }
        return null;
    }
    
    /**
     * get property recrutively
     * @param operatorPropertiesIterator 
     * @param arg
     * @return 
     * @throws OpLogException 
     */
    public synchronized static Object getPropertyRecrutively(
            Iterator<String> operatorPropertiesIterator, 
            Object arg) throws OpLogException {
        String paramName;
        Object result = arg;
        while (operatorPropertiesIterator.hasNext()) {
            paramName = operatorPropertiesIterator.next();
            result = invokeGet(result, paramName);
            result = getPropertyRecrutively(operatorPropertiesIterator, result);
        }
        return result;
    }
}
