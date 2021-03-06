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
package com.github.djbing85.aop;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.github.djbing85.annotation.OpLogJoinPoint;
import com.github.djbing85.aop.handler.IOpLogHandler;
import com.github.djbing85.aop.manage.OpLogJoinPointManager;
import com.github.djbing85.enums.OpLogEnum;
import com.github.djbing85.enums.OpLogSensitiveTypeEnum;
import com.github.djbing85.exception.OpLogException;
import com.github.djbing85.model.DefaultOpLog;
import com.github.djbing85.sensitive.mask.DefaultSensitiveFieldMasker;
import com.github.djbing85.sensitive.mask.ISensitiveFieldMasker;
import com.github.djbing85.utils.OpLogUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Process all method annotated with OpLogJoinPoint, 
 *  get BO before and after, 
 *  then build an OpLog and pass it to corresponding 
 *  <b>self-defined</b> handler via BO.class<p>
 * before --&gt; pre-BO<p>
 * after  --&gt; post-BO<p>
 * handler --&gt; compare the difference<p>
 * @author djbing85@gmail.com
 * @param OpLog DefaultOpLog
 * @param BO BUSINESS MODEL
 * @since 2019-05-28
 */
@Aspect
public abstract class AbstractOpLogAOPInterceptor<OpLog extends DefaultOpLog<BO>, BO> implements ApplicationContextAware {
    
    protected final Logger LOG = Logger.getLogger(this.getClass());

    //TO BE INJECTED
    protected static ApplicationContext applicationContext;  

    protected Multimap<String, IOpLogHandler<BO>> handlersMap = ArrayListMultimap.create();

    protected ISensitiveFieldMasker<BO> defaultSensitiveFieldHandler = new DefaultSensitiveFieldMasker<BO>();

    protected static DateFormat opTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    protected static final String PRE_BO = "preVal";
    
    private static LoadingCache<Integer, Map<String, Object>> cache 
        = CacheBuilder.newBuilder()
            .expireAfterAccess(5000, TimeUnit.MILLISECONDS)
            .expireAfterWrite(10000, TimeUnit.MILLISECONDS)
            .build(new CacheLoader<Integer, Map<String, Object>>(){
                @Override
                public Map<String, Object> load(Integer key) throws Exception {
                    return new HashMap<String, Object>();
                }
            });
    
    /**
     * Get BO before method is invoked<p>
     * clone the BO, because a reference to the same 
     *  object will be changed after method executed<p>
     * place the BO into cache for a later use<p>
     * @param jp JoinPoint
     */
    @SuppressWarnings("unchecked")
    @Before("@annotation(com.github.djbing85.annotation.OpLogJoinPoint)")
    public void pre(JoinPoint jp) {
        try {
            OpLogJoinPointManager<BO> joinPointManager = 
                    new OpLogJoinPointManager<BO>(applicationContext, jp, null, true);
            BO objPre = joinPointManager.getOpLogBusinessObject();
            BO objPreClone = null;
            if(objPre != null) {
                try {
                    objPreClone = (BO)objPre.getClass().newInstance();
                } catch (Exception e) {
                    LOG.error("please define a default constructor for class: " + objPre.getClass().getName() + ". error: " + e.getMessage(), e);
                    return;
                }
                BeanUtils.copyProperties(objPre, objPreClone);
            }
            
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("jp", jp);
            map.put(PRE_BO, objPreClone);
            cache.put(jp.hashCode(), map);
        } catch (Exception e) {
            LOG.error("error when get BO from join point method: "
                    + jp.getSignature().getName() + ". error: " + e.getMessage(), e);
            return;
        }
    }
    
    /**
     * 1. Get pre BO from cache<p>
     * 2. Get post BO after method return<p>
     * 3. Compare two BO<p>
     * 4. Build OpLog, call handler upon corresponding BO.getClass()<p>
     * 
     * @param jp JoinPoint
     * @param rvt method return
     */
    @SuppressWarnings("unchecked")
    @AfterReturning(returning="rvt", pointcut="@annotation(com.github.djbing85.annotation.OpLogJoinPoint)")
    public void post(JoinPoint jp, Object rvt) {
        try {
            Map<String, Object> map = cache.get(jp.hashCode());
            if(map == null) {
                LOG.error("could not find join point preVal(not even null!) from cache: " + jp.hashCode());
                return;
            }
            BO objPre = null;
            // objPre might be null in some cases, especially when in create/insert functions
            if(map.get(PRE_BO) != null) {
                objPre = (BO)map.get(PRE_BO);
            }
            
            OpLogJoinPointManager<BO> joinPointManager = new OpLogJoinPointManager<BO>(applicationContext, jp, rvt, false);

            BO objPost = joinPointManager.getOpLogBusinessObject();
            
            OpLog opLog = buildOpLog(jp, objPre, objPost);
            //LOG.debug(opLog.toString());
            this.handleDiff(opLog);
        } catch (Exception e) {
            LOG.error("error when parse join point: " + jp.getSignature().getName(), e);
            return;
        }
    }
    
    /**
     * Build OpLog
     * @param jp JoinPoint
     * @param pre BO
     * @param post BO
     * @throws OpLogException see e.getMessage() for more detail
     * @return DefaultOpLog
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected OpLog buildOpLog(JoinPoint jp, BO pre, BO post) throws OpLogException {
        if(pre == null && post == null) {
            LOG.warn("pre and post are both null!!!");
            return null;
        }

        DefaultOpLog opLog = new DefaultOpLog();
        if(pre != null) {
            opLog.setPre(JSONObject.toJSONString(pre));
        }
        if(post != null) {
            opLog.setPost(JSONObject.toJSONString(post));
        }
        
        //get opType
        String opType = null;
        if(pre == null && post != null) {
            opType = OpLogEnum.OP_TYPE_CREATE.getDesc();
            opLog.setModelClass(post.getClass());
        } else if(pre != null && post == null) {
            opType = OpLogEnum.OP_TYPE_DELETE.getDesc();
            opLog.setModelClass(pre.getClass());
        } else {
            opType = OpLogEnum.OP_TYPE_UPDATE.getDesc();
            opLog.setModelClass(pre.getClass());
        }
        opLog.setOpType(opType);
        
        //oplog summary, use method name if not specified
        String summary = null;
        //oplog operator, use null if not specified
        String operator = null;
        MethodInvocationProceedingJoinPoint mipjp = (MethodInvocationProceedingJoinPoint) jp;
        MethodSignature methodSignature = (MethodSignature)mipjp.getSignature();
        
        OpLogJoinPoint alJp = methodSignature.getMethod().getAnnotation(OpLogJoinPoint.class);
        
        if(alJp != null) {
            summary = alJp.summary();
            operator = alJp.operator();
        }
        //if annotation does not specify oplog summary, use method name instead
        if(StringUtils.isEmpty(summary)) {
            summary = methodSignature.getMethod().getName();
        }
        opLog.setSummary(summary);
        
        //if no operator specified, set it to null;
        if(StringUtils.isEmpty(operator)) {
            operator = null;
        } else {
            operator = getOperator(jp, operator);
        }
        opLog.setOperator(operator);
        
        //call concrete sub class for model diff
        Object diff = getModelDiff(opLog.getModelClass(), pre, post);
        opLog.setDiff(diff);

        opLog.setOpTime(new Date());
        
        return (OpLog)opLog;
    }
    
    /**
     * Get the operator via JoinPoint annotation config<p>
     * Sample: <p>
     * <code>@OpLogJoinPoint(operator="user.userName")</code><p>
     * <code>public Boolean updateUserProfile(User user){...}</code><p>
     * The userName is a field of arg <b>user</b>, 
     *  and a getter <b>getUserName()</b> should be defined<p/>
     * Or: <p>
     * <code>@OpLogJoinPoint(operator="auditor")</code><p>
     * <code>public Boolean auditUser(Long userId, Integer auditStatus, String auditor){...}</code><p>
     * Will return the argument value of [auditor]
     * 
     * @param jp JoinPoint
     * @param operator raw config
     * @return operator value
     */
    private String getOperator( JoinPoint jp, String operator ) throws OpLogException {
        MethodInvocationProceedingJoinPoint mipjp = (MethodInvocationProceedingJoinPoint) jp;
        MethodSignature methodSignature = (MethodSignature)mipjp.getSignature();
        
        String[] parameters = methodSignature.getParameterNames();
        String [] operatorArray = operator.split("\\.");
        
        Object[] args = jp.getArgs();
        
        // a valid operator annotation should look like user.userName
        Iterator<String> operatorPropertiesIterator = Arrays.asList(operatorArray).iterator();
        Map<String, Object> paramNameArgMap = new HashMap<String, Object>(); 
        for(int i = 0; i < parameters.length; i++) {
            paramNameArgMap.put(parameters[i], args[i]);
        }

        Object arg = null;
        if(operatorPropertiesIterator.hasNext()) {
            String argName = operatorPropertiesIterator.next();
            arg = paramNameArgMap.get(argName);
            if(arg != null) {
                Object obj = OpLogUtils.getPropertyRecrutively(operatorPropertiesIterator, arg);
                return obj == null? null: obj.toString();
            }
        }
        return null;
    }

    protected Object doFieldMapping(JSONObject fieldMap, Object value) {
        if(fieldMap != null && fieldMap.size() > 0) {
            Object mapVal = fieldMap.get(value);
            if(mapVal != null) {
                value = mapVal;
            }
        }
        return value;
    }

    protected Object formatDateField(Class<BO> modelClass, String dateFormat, Object value, Field field) {
        if(StringUtils.isEmpty(dateFormat)) {
            return value;
        }
        if(value == null) {
            return value;
        }
        DateFormat sdf;
        if(value instanceof Date) {
            try {
                sdf = new SimpleDateFormat(dateFormat);
                value = sdf.format((Date) value);
            } catch (Exception e) {
                LOG.error("error formating date " + value + " with given dateFormat: " + dateFormat + 
                        " in BO: " + modelClass + ", field: " + field.getName(), e);
            }
        }
        if(value instanceof Calendar) {
            try {
                sdf = new SimpleDateFormat(dateFormat);
                value = sdf.format(((Calendar) value).getTime());
            } catch (Exception e) {
                LOG.error("error format Calendar " + value + " with given dateFormat: " + dateFormat + 
                        " in BO: " + modelClass + ", field: " + field.getName(), e);
            }
        }
        if(value instanceof LocalDate) {
            try {
                LocalDate tempLocalDate = (LocalDate) value;
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(dateFormat);
                value = formatter1.format(tempLocalDate);
            } catch (Exception e) {
                LOG.error("error format LocalDate" + value + " with given dateFormat: " + dateFormat + 
                        " in BO: " + modelClass + ", field: " + field.getName(), e);
            }
        }
        if(value instanceof LocalTime) {
            try {
                LocalTime tempLocalTime = (LocalTime) value;
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(dateFormat);
                value = formatter1.format(tempLocalTime);
            } catch (Exception e) {
                LOG.error("error format LocalTime" + value + " with given dateFormat: " + dateFormat + 
                        " in BO: " + modelClass + ", field: " + field.getName(), e);
            }
        }
        if(value instanceof LocalDateTime) {
            try {
                LocalDateTime tempLocalDateTime = (LocalDateTime) value;
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(dateFormat);
                value = formatter1.format(tempLocalDateTime);
            } catch (Exception e) {
                LOG.error("error format LocalDateTime" + value + " with given dateFormat: " + dateFormat + 
                        " in BO: " + modelClass + ", field: " + field.getName(), e);
            }
        }
        return value;
    }

    protected JSONObject parseFieldMapping(Class<BO> modelClass, String fieldMapping, Field field) {
        JSONObject fieldMap;
        try {
            if(!StringUtils.isEmpty(fieldMapping)) {
                fieldMap = JSONObject.parseObject(fieldMapping);
            } else {
                fieldMap = new JSONObject();
            }
        } catch (Exception e) {
            LOG.warn("fieldMapping found wrong json format: " + fieldMapping + 
                    " in BO: " + modelClass + ", field: " + field.getName());
            fieldMap = new JSONObject();
        }
        return fieldMap;
    }

    protected Object formatDecimal(Class<BO> modelClass, String decimalFormat, Object value, Field field) {
        if(!StringUtils.isEmpty(decimalFormat)) {
            if(value instanceof BigDecimal || value instanceof Double || 
                    value instanceof Float || value instanceof Long || 
                    value instanceof Integer) {
                try {
                    DecimalFormat df = new DecimalFormat(decimalFormat);
                    value = df.format(value);
                } catch (Exception e) {
                    LOG.error("error format digit with given decimalFormat: " + decimalFormat + 
                            " in BO: " + modelClass + ", field: " + field.getName(), e);
                }
            }
        }
        return value;
    }

    protected Object maskField(Class<BO> modelClass, 
            OpLogSensitiveTypeEnum sensitiveTypeEnum, 
            Object value,
            Field field) {
        try {
            value = defaultSensitiveFieldHandler.maskSensitiveValue(modelClass, value, sensitiveTypeEnum);
        } catch (Exception e) {
            LOG.warn("failed to mask sensitive field: " + field.getName() + 
                    " of class: " + modelClass.getName() + ", error: " + e.getMessage(), e);
        }
        return value;
    }
    
    /**
     * Call handler(s) to persist into outer storage, or output log etc.
     * @param log the op-log
     */
    protected void handleDiff(DefaultOpLog<BO> log) {
        Class<BO> clazz = null;
        if(log != null) {
            clazz = log.getModelClass();
        }
        String className = null;
        if(clazz != null) {
            className = clazz.getName();
        }
        Collection<IOpLogHandler<BO>> handlerList = handlersMap.get(className);
        if(handlerList != null) {
            for(IOpLogHandler<BO> h: handlerList) {
                h.handleDiff(log);
            }
        }
    }

    /**
     * Get the difference between two BO. <p>
     * Override this method to create the styles if you like
     * @param modelClass BO class
     * @param pre BO before method proceed
     * @param post BO after method proceed
     * @throws OpLogException see e.getMessage() for more detail
     * @return difference between two BO, detail is implemented in sub-class
     */
    protected abstract Object getModelDiff(
            Class<BO> modelClass, Object pre, Object post)
                    throws OpLogException;
    
    public void setHandlers(List<IOpLogHandler<BO>> handlers) {
        if(handlersMap == null || handlersMap.size() == 0) {
            for(IOpLogHandler<BO> h: handlers) {
                handlersMap.put(h.getModelClass().getName(), h);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) 
            throws BeansException {
        AbstractOpLogAOPInterceptor.applicationContext = applicationContext;
    }

}