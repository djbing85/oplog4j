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
 * Annotation label on a BO class.<br/>
 * For example:
     com.xxx.Model.User is the BO class, 
         usually this is where we place the annotation: "@OpLogModel" on
     com.xxx.dao.UserDao is the DAO class, 
         and it's corresponding <code>daoBeanId</code> is "userDao"
     com.xxx.dao.UserDao.getById(Long id) is the method that retrieve the corresponding model by id
 * @author djbing85@gmail.com
 * @since 2019-05-07
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface OpLogModel {

    /**
     * DAO bean Id
     */
    String daoBeanId();
    
    /**
     * Method name that return a BO model by id. <br/>
     * Be aware that this method must be the same(equals) with the method name inside the DAO, <br/>
     *  and should be unique in the DAO. <br/>
     * If no, please create a unique one. <br/>
     * For example: com.xxx.dao.UserDao.getById(Long id), 
     *  the method name should be "getById"
     */
    String method();
    
}
