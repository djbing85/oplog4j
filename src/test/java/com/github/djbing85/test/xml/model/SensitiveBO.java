package com.github.djbing85.test.xml.model;

import com.github.djbing85.annotation.OpLogField;
import com.github.djbing85.annotation.OpLogModel;
import com.github.djbing85.enums.OpLogSensitiveTypeEnum;

import lombok.Data;

@Data
@OpLogModel(daoBeanId = "sensitiveBOService", method = "getById")
public class SensitiveBO {
    
    //a default constructor is a must when there is a customized one
    public SensitiveBO() {}

    @OpLogField(id = 0, fieldName = "User ID", fieldMapping = "")
    private Long id;
    @OpLogField(fieldName = "secret", isSensitive = true, maskPattern = OpLogSensitiveTypeEnum.MASK_2SIDES)
    private Long secret;
    @OpLogField(fieldName = "confidential", isSensitive = true, maskPattern = OpLogSensitiveTypeEnum.MASK_PREFIX)
    private Integer confidential;
    @OpLogField(fieldName = "Phone", isSensitive = true, maskPattern = OpLogSensitiveTypeEnum.MASK_SUBFIX)
    private String phone;
    @OpLogField(fieldName = "Password", isSensitive = true, maskPattern = OpLogSensitiveTypeEnum.MASK_MIDDLE)
    private String password;

    @OpLogField(fieldMapping = ", ")
    private String rawFieldName;
    
}
