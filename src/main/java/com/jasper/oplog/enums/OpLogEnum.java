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
package com.jasper.oplog.enums;

public enum OpLogEnum {
    OP_TYPE_CREATE(1, "CREATE"),
    OP_TYPE_SELECT(2, "SELECT"),
    OP_TYPE_UPDATE(3, "UPDATE"),
    OP_TYPE_DELETE(4, "DELETE");

    private final Integer value;
    private final String desc;

    OpLogEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static OpLogEnum valueOf(Integer value) {
        for (OpLogEnum status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + value + "]");
    }

    @Override
    public String toString() {
        return "value " + value + ", desc " + desc;
    }

}
