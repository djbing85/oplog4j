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

import java.io.Serializable;
import java.util.Date;

/**
 * @author djbing85@gmail.com
 * @since 2019-05-27
 */
public class DefaultOpLog<BO> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -6122933091561834110L;

    public DefaultOpLog() {}
    
    public DefaultOpLog(BO pre, BO post, String opType, String summary, 
            String operator, String diff, Date opTime) {
        super();
        this.pre = pre;
        this.post = post;
        this.opType = opType;
        this.summary = summary;
        this.operator = operator;
        this.diff = diff;
        this.opTime = opTime;
    }

    private Object pre;

    private Object post;
    

    private String opType;

    private String summary;
    
    private String operator;
    
    private Class<BO> modelClass;

    private Object diff;

    private Date opTime;

    public Object getPre() {
        return pre;
    }

    public void setPre(Object pre) {
        this.pre = pre;
    }

    public Object getPost() {
        return post;
    }

    public void setPost(Object post) {
        this.post = post;
    }
    

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }
    
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Class<BO> getModelClass() {
        return modelClass;
    }

    public void setModelClass(Class<BO> modelClass) {
        this.modelClass = modelClass;
    }

    public Object getDiff() {
        return diff;
    }

    public void setDiff(Object diff) {
        this.diff = diff;
    }

    public Date getOpTime() {
        return opTime;
    }

    public void setOpTime(Date opTime) {
        this.opTime = opTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DefaultOpLog [");
        builder.append("pre=");
        builder.append(pre);
        builder.append(", post=");
        builder.append(post);
        builder.append(",opType=");
        builder.append(opType);
        builder.append(", summary=");
        builder.append(summary);
        builder.append(", operator=");
        builder.append(operator);
        builder.append(", modelClass=");
        builder.append(modelClass);
        builder.append(", diff=");
        builder.append(diff);
        builder.append(", opTime=");
        builder.append(opTime);
        builder.append("]");
        return builder.toString();
    }
}
