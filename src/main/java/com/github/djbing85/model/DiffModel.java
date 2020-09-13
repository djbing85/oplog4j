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
package com.github.djbing85.model;

import java.util.List;

import lombok.Data;

/**
 * Diff model
 * @author djbing85@gmail.com
 * @since 2020-09-09
 */
@Data
public class DiffModel {
    
    /** field name */
    private String fieldName;
    /** field value : from */
    private String from;
    /** field value : to */
    private String to;
    /** sub model which is/are annotated by OpLogModel */
    private List<DiffModel> subModelDiffList;
}
