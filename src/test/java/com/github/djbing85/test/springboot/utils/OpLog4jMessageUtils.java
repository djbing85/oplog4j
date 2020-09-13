package com.github.djbing85.test.springboot.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import com.github.djbing85.model.DiffModel;

public class OpLog4jMessageUtils {

   private static MessageSource messageSource;

   public OpLog4jMessageUtils(MessageSource messageSource) {
       OpLog4jMessageUtils.messageSource = messageSource;
   }

   /**
    * 
    */
   public static String get(String msgKey) {
       try {
           return messageSource.getMessage(msgKey, null, LocaleContextHolder.getLocale());
       } catch (Exception e) {
           return msgKey;
       }
   }

   /** map the value by current locale, check source code of DiffModel for more detail */
   public static void i18nFieldName(DiffModel dm) {
       if(dm == null || StringUtils.isEmpty(dm.getFieldName())) {
           return;
       }
       dm.setFieldName(OpLog4jMessageUtils.get(dm.getFieldName()));

       if(dm.getSubModelDiffList() == null || dm.getSubModelDiffList().size() == 0) {
           return;
       }
       for(DiffModel subDm: dm.getSubModelDiffList()) {
           i18nFieldName(subDm);
       }
   }
}
