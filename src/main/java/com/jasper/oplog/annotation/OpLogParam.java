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
package com.jasper.oplog.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Label a parameter in the method as a BO/POJO/MODEL.<br/>
 * This usually apply on methods like: <br/>
 *  <code>com.xxx.service.UserService.update(User u);</code><br/>
 * In this case, we add annotation as below:<br/>
 *  <code>com.xxx.service.UserService.update(@OpLogParam User u);</code>
 * @author djbing85@gmail.com
 * @since 2019-05-27
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface OpLogParam {
    
    /**
     * true : to use this BO parameter as pre-BO directly;<br/>
     * false: need to load from outer storage
     */
    boolean isLoaded() default false;
}
