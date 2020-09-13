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
package com.github.djbing85.aop.handler;

import com.github.djbing85.model.DefaultOpLog;

/**
 * @author djbing85@gmail.com
 * @since 2019-06-12
 * @param <BO> BUSINESS OBJECT
 */
public interface IOpLogHandler<BO> {

    /**
     * Persist log 
     * @param log
     */
    void handleDiff(DefaultOpLog<BO> log);

    /**
     * Each BO should have it's corresponding class register here
     * @return
     */
    Class<BO> getModelClass();
}
