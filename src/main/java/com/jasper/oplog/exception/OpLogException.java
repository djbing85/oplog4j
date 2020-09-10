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
package com.jasper.oplog.exception;


/**
 * @author djbing85@gmail.com
 * @since 2019-05-27
 */
public class OpLogException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6278785170227461848L;

    public OpLogException(String message) {
        super(message);
    }

    public OpLogException(String message, Throwable cause) {
        super(message, cause);
    }
}
