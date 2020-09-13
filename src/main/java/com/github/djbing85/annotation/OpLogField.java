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
package com.github.djbing85.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.djbing85.enums.OpLogSensitiveTypeEnum;

/**
 * Label the fields in the BO/POJO/MODEL bean<br/>
 * @author djbing85@gmail.com
 * @since 2019-05-07
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface OpLogField {

    /**
     * Id indexes. default -1 means not an ID field.
     * If it is a composed id, 
     * the first id field is 0, the second id field is 1, 
     * other id must match the orders in a method like getById(id0, id1..., idN)
     */
    int id() default -1;
    
    /**
     * BO field name in a human-friendly way, 
     * default/empty field name in a BO means to generate oplog with it's raw field name
     */
    String fieldName() default "";
    
    /**
     * JSON string mapper<br/>
     * A typical value should look like: <br/>
     * ---- {"0": "disabled", "1": "enabled"} ----<br/>
     * Map the field values like [0/1] to a human-readable string like [enabled/disabled] <br/>
     * Oplog intercepter will take the field value as key, 
     * "translate" it to the mapped value while generating the oplog automatically<br/>
     * Default or empty means field value will be used directly<br/>
     * A malformed JSON string will result to a false translate: that is, to use raw value.
     */
    String fieldMapping() default "{}";
    
    /**
     * Specify if the field should be ignored when generate oplog detail<br/>
     * An ignore field will be skip when compare differences between two BO object<br/>
     */
    boolean ignore() default false;

    /**
     * A sensitive field like password, identity ID, bank account No. etc will be half-marked as ** when generate oplog<br/>
     * For example, raw value: password=12345678 will be displayed as password=1234** in the oplog diff result<br/>
     * For more detail @See maskPattern and  @See OpLogSensitiveTypeEnum
     */
    boolean isSensitive() default false;
    
    /**
     * Works only when isSensitive == true
     * For more detail @See OpLogSensitiveTypeEnum
     */
    OpLogSensitiveTypeEnum maskPattern() default OpLogSensitiveTypeEnum.MASK_ALL;

    /**
     * Date format will only apply on field type list below: <br/>
     * java.util.Date<br/>
     * java.util.Calendar<br/>
     * java.time.LocalDate<br/>
     * java.time.LocalTime<br/>
     * java.time.LocalDateTime<br/>
     * Invalid date format will result to a format failure,<br/>
     */
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";
    
    /**
     * A valid decimalFormat should looks like: #,###.##<br/>
     * Will try to format Double/Float/Long/Integer/BigDecimal fields <br/>
     * Empty decimalFormat will be ignore<br/>
     * <p>
     * For example: #,###.## <br/>
     * Double d = 554545.4545454; <br/>
     * Formatted String: 554,545.45; <br/>
     * Long l = 1234567890;<br/>
     * Formatted String: 1,234,567,890<br/>
     */
    String decimalFormat() default "";
    
}
