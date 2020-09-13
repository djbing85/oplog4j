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
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.github.djbing85.annotation.OpLogField;
import com.github.djbing85.annotation.OpLogModel;
import com.github.djbing85.enums.OpLogSensitiveTypeEnum;
import com.github.djbing85.exception.OpLogException;
import com.github.djbing85.model.DefaultOpLog;
import com.github.djbing85.model.DiffModel;
import com.github.djbing85.utils.OpLogUtils;

/**
 * @author djbing85@gmail.com
 * @param <BO>
 * @since 2020-09-08
 */
@Aspect
public class JsonDiffOpLogAOPInterceptor<OpLog extends DefaultOpLog<BO>, BO> 
        extends AbstractOpLogAOPInterceptor<DefaultOpLog<BO>, BO> {
    
    protected final Logger LOG = Logger.getLogger(this.getClass());
    
    /**
     * Create or Delete, will have only pre-BO or post-BO, 
     *  in those cases we need this method to generate opLog;
     * @param modelClass bean annotated by OpLogModel
     * @param obj: bean instance
     * @return translate the obj base on the annotation
     * @throws OpLogException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<DiffModel> transferSingleModel(Class<BO> modelClass, Object obj, boolean isFrom) throws OpLogException {
        List<DiffModel> diffModelList = new ArrayList<>();
        DiffModel diffModel;
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
        
        for (Field field : fields) {
            diffModel = new DiffModel();
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

                value = OpLogUtils.invokeGet(obj, field.getName());
                if(value == null) {
                    continue;
                }
                
                diffModel.setFieldName(fieldName);

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
                    List<DiffModel> subModelDiffList = null; 
                    if(value != null) {
                        Object objDiff = this.transferSingleModel(subModelClz, value, isFrom);
                        if(objDiff != null) {
                            subModelDiffList = (List<DiffModel>)objDiff;
                        }
                        
                    }
                    if(subModelDiffList != null && subModelDiffList.size() > 0) {
                        diffModel.setSubModelDiffList(subModelDiffList);
                        diffModelList.add(diffModel);
                    }
                    //those fields does not need to set value
                    continue;
                }
                
                //TODO list
//                ParameterizedType type = (ParameterizedType) field.getGenericType();
//                Class<?> actualTypeArgument = (Class<?>)type.getActualTypeArguments()[0];
                
                if(isFrom) {
                    diffModel.setFrom(value == null? null: value.toString());
                } else {
                    
                }
                
                diffModelList.add(diffModel);
            }
        }
        
        return diffModelList;
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
    @SuppressWarnings("unchecked")
    protected List<DiffModel> getModelDiff(Class<BO> modelClass, Object pre, Object post) 
                    throws OpLogException {
        List<DiffModel> diffModelList = new ArrayList<>();
        DiffModel diffModel = new DiffModel();

        if(pre != null && post == null) {
            diffModel.setFrom(JSONObject.toJSONString(transferSingleModel(modelClass, pre, true)));
            diffModelList.add(diffModel);
            return diffModelList;
        } 
        if(post != null && pre == null) {
            diffModel.setTo(JSONObject.toJSONString(transferSingleModel(modelClass, post, false)));
            diffModelList.add(diffModel);
            return diffModelList;
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
        DateFormat sdf;
        
        for (Field field : fields) {
            diffModel = new DiffModel();
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
                try {
                    if(!StringUtils.isEmpty(fieldMapping)) {
                        fieldMap = JSONObject.parseObject(fieldMapping);
                    } else {
                        fieldMap = new JSONObject();
                    }
                } catch (Exception e) {
                    LOG.warn("wrong json format: " + fieldMapping);
                    fieldMap = new JSONObject();
                }
                
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
                
                diffModel.setFieldName(fieldName);
                if(!StringUtils.isEmpty(dateFormat)) {
                    if(field.getType().equals(Date.class)) {
                        try {
                            sdf = new SimpleDateFormat(dateFormat);
                            if(valuePre != null) {
                                valuePre = sdf.format((Date) valuePre);
                            }
                            if(valuePost != null) {
                                valuePost = sdf.format((Date) valuePost);
                            }
                        } catch (Exception e) {
                            LOG.error("error format date with given OpLogField.dateFormat: " + dateFormat +
                                    ". valuePre: " + valuePre + ", valuePost: " + valuePost, e);
                        }
                    }
                    if(field.getType().equals(Calendar.class)) {
                        try {
                            sdf = new SimpleDateFormat(dateFormat);
                            if(valuePre != null) {
                                valuePre = sdf.format(((Calendar) valuePre).getTime());
                            }
                            if(valuePost != null) {
                                valuePost = sdf.format(((Calendar) valuePost).getTime());
                            }
                        } catch (Exception e) {
                            LOG.error("error format Calendar with given OpLogField.dateFormat: " + dateFormat +
                                    ". valuePre: " + valuePre + ", valuePost: " + valuePost, e);
                        }
                    }
                    if(field.getType().equals(LocalDate.class)) {
                        try {
                            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(dateFormat);
                            LocalDate tempLocalDate = null;
                            if(valuePre != null) {
                                tempLocalDate = (LocalDate) valuePre;
                                valuePre = formatter1.format(tempLocalDate);
                            }
                            if(valuePost != null) {
                                tempLocalDate = (LocalDate) valuePost;
                                valuePost = formatter1.format(tempLocalDate);
                            }
                        } catch (Exception e) {
                            LOG.error("error format LocalDate with given OpLogField.dateFormat: " + dateFormat +
                                    ". valuePre: " + valuePre + ", valuePost: " + valuePost, e);
                        }
                    }
                    if(field.getType().equals(LocalTime.class)) {
                        try {
                            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(dateFormat);
                            LocalTime tempLocalTime = null;
                            if(valuePre != null) {
                                tempLocalTime = (LocalTime) valuePre;
                                valuePre = formatter1.format(tempLocalTime);
                            }
                            if(valuePost != null) {
                                tempLocalTime = (LocalTime) valuePost;
                                valuePost = formatter1.format(tempLocalTime);
                            }
                        } catch (Exception e) {
                            LOG.error("error format LocalTime with given OpLogField.dateFormat: " + dateFormat +
                                    ". valuePre: " + valuePre + ", valuePost: " + valuePost, e);
                        }
                    }
                    if(field.getType().equals(LocalDateTime.class)) {
                        try {
                            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern(dateFormat);
                            LocalDateTime tempLocalDateTime = null;
                            if(valuePre != null) {
                                tempLocalDateTime = (LocalDateTime) valuePre;
                                valuePre = formatter1.format(tempLocalDateTime);
                            }
                            if(valuePost != null) {
                                tempLocalDateTime = (LocalDateTime) valuePost;
                                valuePost = formatter1.format(tempLocalDateTime);
                            }
                        } catch (Exception e) {
                            LOG.error("error format tempLocalDateTime with given OpLogField.dateFormat: " + dateFormat +
                                    ". valuePre: " + valuePre + ", valuePost: " + valuePost, e);
                        }
                    }
                }
                if(!StringUtils.isEmpty(decimalFormat)) {
                    if(field.getType().equals(BigDecimal.class) || field.getType().equals(Double.class) || 
                            field.getType().equals(Float.class) || field.getType().equals(Long.class) || 
                            field.getType().equals(Integer.class)) {
                        try {
                            DecimalFormat df = new DecimalFormat(decimalFormat);
                            if(valuePre != null) {
                                valuePre = df.format(valuePre);
                            }
                            if(valuePost != null) {
                                valuePost = df.format(valuePost);
                            }
                        } catch (Exception e) {
                            LOG.error("error format digit with given OpLogField.decimalFormat: " + decimalFormat, e);
                        }
                    }
                }
                
                if(fieldMap != null && fieldMap.size() > 0) {
                    Object mapValPre = fieldMap.get(valuePre);
                    if(mapValPre != null) {
                        valuePre = mapValPre;
                    }
                    Object mapValPost = fieldMap.get(valuePost);
                    if(mapValPost != null) {
                        valuePost = mapValPost;
                    }
                }
                
                if(isSensitive) {
                    valuePre = maskField(modelClass, sensitiveTypeEnum, valuePre, field);
                    valuePost = maskField(modelClass, sensitiveTypeEnum, valuePost, field);
                }

                @SuppressWarnings("rawtypes")
                Class subModelClz = null;
                if(valuePre != null) {
                    subModelClz = valuePre.getClass();
                }
                if(valuePost != null) {
                    subModelClz = valuePost.getClass();
                }
                Annotation opLogModelAnno = subModelClz.getAnnotation(OpLogModel.class);
                if(opLogModelAnno != null) {
                    List<DiffModel> subModelDiffList = null;
                    if(valuePre != null && valuePost != null) {
                        Object objDiff = this.getModelDiff(subModelClz, valuePre, valuePost);
                        if(objDiff != null) {
                            subModelDiffList = (List<DiffModel>)objDiff;
                        }
                    } else if(valuePre == null && valuePost != null) {
                        Object objDiff = this.transferSingleModel(subModelClz, valuePost, false);
                        if(objDiff != null) {
                            subModelDiffList = (List<DiffModel>)objDiff;
                        }
                    } else if(valuePre != null && valuePost == null) {
                        Object objDiff = this.transferSingleModel(subModelClz, valuePre, true);
                        if(objDiff != null) {
                            subModelDiffList = (List<DiffModel>)objDiff;
                        }
                    }
                    if(subModelDiffList != null && subModelDiffList.size() > 0) {
//                        sb.append("\n").append(subModelDiff).append("\n");
                        diffModel.setSubModelDiffList(subModelDiffList);
                        diffModelList.add(diffModel);
                    }
                    continue;
                }

//                sb.append(valuePre).append(" --> ").append(valuePost).append("\n");
                diffModel.setFrom(JSONObject.toJSONString(valuePre));
                diffModel.setTo(JSONObject.toJSONString(valuePost));
                diffModelList.add(diffModel);
            }
        }
        
//        if(sb.length() > 1 && sb.toString().endsWith("\n")) {
//            return sb.substring(0, sb.length() - 1);
//        }

        return diffModelList;
    }


}