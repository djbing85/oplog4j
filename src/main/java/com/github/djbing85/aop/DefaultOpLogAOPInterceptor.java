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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.github.djbing85.annotation.OpLogField;
import com.github.djbing85.annotation.OpLogModel;
import com.github.djbing85.aop.handler.IOpLogHandler;
import com.github.djbing85.enums.OpLogSensitiveTypeEnum;
import com.github.djbing85.exception.OpLogException;
import com.github.djbing85.model.DefaultOpLog;
import com.github.djbing85.sensitive.mask.DefaultSensitiveFieldMasker;
import com.github.djbing85.sensitive.mask.ISensitiveFieldMasker;
import com.github.djbing85.utils.OpLogUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @author djbing85@gmail.com
 * @param <BO>
 * @since 2019-05-10
 */
@Aspect
public class DefaultOpLogAOPInterceptor<OpLog extends DefaultOpLog<BO>, BO> 
        extends AbstractOpLogAOPInterceptor<DefaultOpLog<BO>, BO> {
    
    protected final Logger LOG = Logger.getLogger(this.getClass());

    protected Multimap<String, IOpLogHandler<BO>> handlersMap = ArrayListMultimap.create();

    protected ISensitiveFieldMasker<BO> defaultSensitiveFieldHandler = new DefaultSensitiveFieldMasker<BO>();

    protected Map<String, ISensitiveFieldMasker<BO>> sensitiveFieldhandlersMap = new HashMap<String, ISensitiveFieldMasker<BO>>();

    protected String indent;
    
    protected final static String INDENT_APPENDER = "    ";// 4 space
            
    /**
     * Create or Delete, will have only pre-BO or post-BO, 
     *  in those cases we need this method to generate opLog;
     * @param modelClass bean annotated by OpLogModel
     * @param obj: bean instance
     * @return translate the obj base on the annotation
     * @throws OpLogException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object transferSingleModel(Class<BO> modelClass, Object obj) throws OpLogException {
        StringBuilder sb = new StringBuilder();
        OpLogField opLogFieldAnnotation = null;
        Field[] fields = obj.getClass().getDeclaredFields();
        boolean hasAnnotation = false;
        Boolean ignore = false;
        String fieldName;
        Boolean isSensitive = false;
        OpLogSensitiveTypeEnum sensitiveTypeEnum = OpLogSensitiveTypeEnum.MASK_SUBFIX;
        String fieldMapping;
        String dateFormat;
        String decimalFormat;
        JSONObject fieldMap;
        
        Object value;
        indent = indent + INDENT_APPENDER;
        
        for (Field field : fields) {
            hasAnnotation = field.isAnnotationPresent(OpLogField.class);
            if (hasAnnotation) {
                opLogFieldAnnotation = field.getAnnotation(OpLogField.class);
                //
                ignore = opLogFieldAnnotation.ignore();
                if(ignore) {
                    //skip the ignore fields
                    continue;
                }
                fieldName = opLogFieldAnnotation.fieldName();
                if(StringUtils.isEmpty(fieldName)) {
                    //default fieldName
                    fieldName = field.getName();
                }
                isSensitive = opLogFieldAnnotation.isSensitive();
                sensitiveTypeEnum = opLogFieldAnnotation.maskPattern();
                dateFormat = opLogFieldAnnotation.dateFormat();
                decimalFormat = opLogFieldAnnotation.decimalFormat();
                fieldMapping = opLogFieldAnnotation.fieldMapping();
                
                value = OpLogUtils.invokeGet(obj, field.getName());
                if(value == null) {
                    continue;
                }
                
                sb.append(indent).append(fieldName).append(": ");
                
                value = formatDateField(modelClass, dateFormat, value, field);
                
                value = formatDecimal(modelClass, decimalFormat, value, field);
                
                fieldMap = parseFieldMapping(modelClass, fieldMapping, field);
                
                value = doFieldMapping(fieldMap, value);
                
                if(isSensitive) {
                    value = maskField(modelClass, sensitiveTypeEnum, value, field);
                }
                
                Class subModelClz = null;
                if(value != null) {
                    subModelClz = value.getClass();
                }
                Annotation opLogModelAnno = subModelClz.getAnnotation(OpLogModel.class);
                if(opLogModelAnno != null) {
                    String subModelDiff = null;
                    if(value != null) {
                        Object objDiff = this.transferSingleModel(subModelClz, value);
                        if(objDiff != null) {
                            subModelDiff = (String)objDiff;
                        }
                        
                        indent = indent.substring(4);
                    }
                    if(!StringUtils.isEmpty(subModelDiff)) {
                        sb.append("\n").append(subModelDiff).append("\n");
                    }
                }
                
                //TODO list
//                ParameterizedType type = (ParameterizedType) field.getGenericType();
//                Class<?> actualTypeArgument = (Class<?>)type.getActualTypeArguments()[0];
                
                sb.append(value).append("\n");
            }
        }

        if(sb.length() > 1 && sb.toString().endsWith("\n")) {
            return sb.substring(0, sb.length() - 1);
        }
        
        return sb.toString();
    }
    
    /** 
     * @param modelClass BO class
     * @param pre value before method proceed
     * @param post value after method proceed
     * get the difference between two param<br/>
     * @return
     * user name: jim --> lily<br/>
     * balance 10000 --> 0<br/>
     * override this method to create the styles you like
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object getModelDiff(Class<BO> modelClass, Object pre, Object post) 
                    throws OpLogException {
        indent = "";
        StringBuilder sb = new StringBuilder();

        if(pre != null && post == null) {
            sb.append("post is null\npre:").append(transferSingleModel(modelClass, pre));
            indent = indent.substring(4);
            return sb.toString();
        } 
        if(post != null && pre == null) {
            sb.append("pre is null\npost:").append(transferSingleModel(modelClass, post));
            indent = indent.substring(4);
            return sb.toString();
        }
        
        OpLogField opLogFieldAnnotation = null;
        Field[] fields = null;
        if(pre != null) {
            fields = pre.getClass().getDeclaredFields();
        } else if(post != null) {
            fields = post.getClass().getDeclaredFields();
        } else {
            return null;
        }
        
        boolean hasAnnotation = false;
        Boolean ignore = false;
        String fieldName;
        Boolean isSensitive = false;
        OpLogSensitiveTypeEnum sensitiveTypeEnum = OpLogSensitiveTypeEnum.MASK_SUBFIX;
        String fieldMapping;
        String dateFormat;
        String decimalFormat;
        JSONObject fieldMap;
        
        Object valuePre;
        Object valuePost;
        indent = indent + INDENT_APPENDER;
        
        for (Field field : fields) {
            hasAnnotation = field.isAnnotationPresent(OpLogField.class);
            if (hasAnnotation) {
                opLogFieldAnnotation = field.getAnnotation(OpLogField.class);
                //
                ignore = opLogFieldAnnotation.ignore();
                if(ignore) {
                    //skip the ignore fields
                    continue;
                }
                fieldName = opLogFieldAnnotation.fieldName();
                if(StringUtils.isEmpty(fieldName)) {
                    fieldName = field.getName();
                }
                
                isSensitive = opLogFieldAnnotation.isSensitive();
                sensitiveTypeEnum = opLogFieldAnnotation.maskPattern();
                dateFormat = opLogFieldAnnotation.dateFormat();
                decimalFormat = opLogFieldAnnotation.decimalFormat();
                fieldMapping = opLogFieldAnnotation.fieldMapping();
                
                valuePre = OpLogUtils.invokeGet(pre, field.getName());
                valuePost = OpLogUtils.invokeGet(post, field.getName());
                
                if(valuePre == null && valuePost == null) {
                    continue;
                }
                //OpLogModel should have it's own equals method.
                if(valuePre != null && valuePost != null) {
                    if(valuePre.equals(valuePost)) {
                        continue;
                    }
                }
                
                valuePre = formatDateField(modelClass, dateFormat, valuePre, field);
                valuePost = formatDateField(modelClass, dateFormat, valuePost, field);

                valuePre = formatDecimal(modelClass, decimalFormat, valuePre, field);
                valuePost = formatDecimal(modelClass, decimalFormat, valuePost, field);

                fieldMap = parseFieldMapping(modelClass, fieldMapping, field);

                valuePre = doFieldMapping(fieldMap, valuePre);
                valuePost = doFieldMapping(fieldMap, valuePost);
                
                if(isSensitive) {
                    valuePre = maskField(modelClass, sensitiveTypeEnum, valuePre, field);
                    valuePost = maskField(modelClass, sensitiveTypeEnum, valuePost, field);
                }
                
                Class subModelClz = null;
                if(valuePre != null) {
                    subModelClz = valuePre.getClass();
                }
                if(valuePost != null) {
                    subModelClz = valuePost.getClass();
                }
                Annotation opLogModelAnno = subModelClz.getAnnotation(OpLogModel.class);
                if(opLogModelAnno != null) {
                    String subModelDiff = null;
                    if(valuePre != null && valuePost != null) {
                        Object objDiff = this.getModelDiff(subModelClz, valuePre, valuePost);
                        if(objDiff != null) {
                            subModelDiff = (String)objDiff;
                        }
                        indent = indent.substring(4);
                    } else if(valuePre == null && valuePost != null) {
                        Object objDiff = this.transferSingleModel(subModelClz, valuePost);
                        if(objDiff != null) {
                            subModelDiff = (String)objDiff;
                        }
                        indent = indent.substring(4);
                    } else if(valuePre != null && valuePost == null) {
                        Object objDiff = this.transferSingleModel(subModelClz, valuePre);
                        if(objDiff != null) {
                            subModelDiff = (String)objDiff;
                        }
                        indent = indent.substring(4);
                    }
                    if(!StringUtils.isEmpty(subModelDiff)) {
                        sb.append(fieldName).append(": ")
                            .append("\n").append(subModelDiff).append("\n");
                    }
                    continue;
                }

                sb.append(fieldName).append(": ")  
                    .append(valuePre).append(" --> ").append(valuePost).append("\n");
            }
        }
        
        if(sb.length() > 1 && sb.toString().endsWith("\n")) {
            return sb.substring(0, sb.length() - 1);
        }
        
        return sb.toString();
    }

}