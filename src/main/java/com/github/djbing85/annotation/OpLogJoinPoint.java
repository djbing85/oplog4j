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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put this annotation on the method where you wants to generate oplog
 * @author djbing85@gmail.com
 * @since 2019-05-07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( ElementType.METHOD )
public @interface OpLogJoinPoint {

    /**
     * Specify the summary while generate the oplog;<p>
     * Usually this describes what exactly the join point method is doing.<p>
     * For like if the method insert a new row to the USER table in the DB, 
     *  then summary should look like "New user register"<p>
     * Will use join point method name if this is empty<p>
     * @return Join point method brief introduction
     */
    String summary();

    /**
     * Specify the operator in EL expression style, 
     *  a sample value would looks like: "user.userName"<p>
     * Will first try to find in the param list if any parameter name matches 'user', <p>
     *  if the param exist, try to use reflection to call getUserName() on 'user' object,<p>
     * and then return;
     * 
     * @see com.github.djbing85.aop.AbstractOpLogAOPInterceptor#getOperator(JoinPoint, String)
     * @return a EL expression
     */
    String operator() default "";
    
    /**
     * If join point method is using primary key like <code>void cancelById(Long id)</code>, <p>
     *  it is a must to specify a target model Class of the given PK <code>id</code>, <p>
     *  in order to load the BO via it's corresponding DAO
     * Must work with <code>OpLogID</code>
     * @return Target model Class
     */
    Class<?> modelClass() default Void.class;

    /**
     * To generate op-log, we need to compare the same BO before and after it's altered.<p>
     * We define the BO before altered: pre-BO, and the BO after altered: post-BO.<p>
     * If this is set to TRUE, post-BO will use return value directly, 
     *  other than to load from DAO one more time. <p>
     * Surely we will first compare the object type between 
     *  the return value and the target MODEL class, <p>
     * a type mismatch will result to an OpLogException<p>
     * @return default false
     */
    boolean useReturn() default false;
}
