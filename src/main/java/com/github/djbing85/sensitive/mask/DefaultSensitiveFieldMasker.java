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
package com.github.djbing85.sensitive.mask;

import com.github.djbing85.enums.OpLogSensitiveTypeEnum;

public class DefaultSensitiveFieldMasker<BO> implements ISensitiveFieldMasker<BO> {

    /**
     * Only String/Long/Integer fields are supported<br>
     * @param value
     * @param OpLogSensitiveTypeEnum
     * @return replace half or more chars on the given value to * base on the OpLogSensitiveTypeEnum
     */
    @Override
    public Object maskSensitiveValue(Class<BO> modelClass, Object value, OpLogSensitiveTypeEnum sensitiveTypeEnum) {
        if(value == null) {
            return null;
        }
        
        String val;
        if(value instanceof String ||
                value instanceof Long ||
                value instanceof Integer
                ) {
            val = value.toString();
            if(val.length() == 0) {
                return val;
            }
        }
        else {
            return value;
        }
        
        switch(sensitiveTypeEnum) {
        case MASK_PREFIX:
            return maskPrefix(val);
        case MASK_SUBFIX:
            return maskSubfix(val);
        case MASK_MIDDLE:
            return maskMiddle(val);
        case MASK_2SIDES:
            return mask2Sides(val);
        case MASK_ALL:
            return maskAll(val);
        }
        return val;
    }
    
    private static String maskSubfix(String val) {
        if(val.length() == 1) {
            val = "*";
        } else if(val.length() == 2) {
            val = val.substring(0, 1) + "*";
        } else if(val.length() == 3) {
            val = val.substring(0, 1) + "**";
        } else if(val.length() >= 4) {
            Integer halfLen = val.length() / 2;
            val = val.substring(0, halfLen) + "**";
        }
        return val;
    }
    
    private static String maskPrefix(String val) {
        if(val.length() == 1) {
            val = "*";
        } else if(val.length() == 2) {
            val = "*" + val.substring(1);
        } else if(val.length() == 3) {
            val = "**" + val.substring(2);
        } else if(val.length() >= 4) {
            Integer halfLen = val.length() / 2;
            val = "**" + val.substring(halfLen);
        }
        return val;
    }
    
    private static String mask2Sides(String val) {
        if(val.length() == 1) {
            val = "*";
        } else if(val.length() == 2) {
            val = "*" + val.substring(1);
        } else if(val.length() == 3) {
            val = "*" + val.substring(1, 1) + "*";
        } else if(val.length() == 4) {
            val = "*" + val.substring(1, 2) + "*";
        } else if(val.length() == 5) {
            val = "*" + val.substring(1, 2) + "*";
        } else if(val.length() >= 6) {
            Integer start = val.length() / 3;
            Integer end = start<<1;
            val = "**" + val.substring(start, end) + "**";
        }
        return val;
    }

    
    private static String maskMiddle(String val) {
        if(val.length() == 1) {
            val = "*";
        } else if(val.length() == 2) {
            val = "*" + val.substring(1);
        } else if(val.length() == 3) {
            val = val.substring(0, 0) + "*" + val.substring(2);
        } else if(val.length() == 4) {
            val = val.substring(0, 0) + "*" + val.substring(3);
        } else if(val.length() == 5) {
            val = val.substring(0, 0) + "*" + val.substring(4);
        } else if(val.length() >= 6) {
            Integer start = val.length() / 3;
            Integer end = start<<1;
            val = val.substring(0, start) + "**" + val.substring(end);
        }
        return val;
    }

    
    private static String maskAll(String val) {
        if(val.length() == 1) {
            val = "*";
        } else if(val.length() == 2) {
            val = "**";
        } else if(val.length() >= 3) {
            val = "***";
        }
        return val;
    }

}
