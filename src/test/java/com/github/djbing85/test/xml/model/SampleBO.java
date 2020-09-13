package com.github.djbing85.test.xml.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.github.djbing85.annotation.OpLogField;
import com.github.djbing85.annotation.OpLogModel;
import com.github.djbing85.enums.OpLogSensitiveTypeEnum;

/**
 * @author djbing85@gmail.com
 * @since 2019-05-27
 */
@OpLogModel(daoBeanId = "opLogSampleDao", method = "getById")
public class SampleBO implements Serializable{

    /** */
    private static final long serialVersionUID = -6122933091561834110L;

    public SampleBO() {}
    
    @OpLogField(fieldName = "is enable")
    private Boolean boolIsEnable;
    @OpLogField(fieldName = "user type(char)")
    private Character charUserType;
    @OpLogField(ignore = false)
    private Byte byteField;
    @OpLogField(ignore = false)
    private Short shortField;
    @OpLogField(decimalFormat = "#,###")
    private Integer intField;
    @OpLogField(id = 0, fieldName = "ID")
    private Long longId;
    @OpLogField(fieldName = "name")
    private String strName;

    @OpLogField(isSensitive = true, maskPattern = OpLogSensitiveTypeEnum.MASK_SUBFIX)
    private String strPhone;

    @OpLogField(fieldName = "mortgage", decimalFormat = "#,###.##")
    private Float floatMortgage;
    @OpLogField(fieldName = "balance", decimalFormat = "#,###.##")
    private Double doubleBalance;
    @OpLogField(fieldName = "Line of credit", decimalFormat = "###,###.##")
    private BigDecimal bigDecimalLineOfCredit;
    @OpLogField(fieldName = "create date", dateFormat = "yyyy-MM-dd")
    private Date dateCreateDate;
    @OpLogField(fieldName = "last login time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date dateLastLoginTime;

    @OpLogField(fieldName = "user type(string)", fieldMapping = "{\"p\":\"personal\", \"e\":\"enterprise\"}")
    private String strUserType;
    @OpLogField(fieldName = "password", isSensitive = true)
    private String strPsd;
    @OpLogField(ignore = true)
    private String ignoreField;

    public Boolean getBoolIsEnable() {
        return boolIsEnable;
    }
    public void setBoolIsEnable(Boolean boolIsEnable) {
        this.boolIsEnable = boolIsEnable;
    }
    public Character getCharUserType() {
        return charUserType;
    }
    public void setCharUserType(Character charUserType) {
        this.charUserType = charUserType;
    }
    public Byte getByteField() {
        return byteField;
    }
    public void setByteField(Byte byteField) {
        this.byteField = byteField;
    }
    public Short getShortField() {
        return shortField;
    }
    public void setShortField(Short shortField) {
        this.shortField = shortField;
    }
    public Integer getIntField() {
        return intField;
    }
    public void setIntField(Integer intField) {
        this.intField = intField;
    }
    public Long getLongId() {
        return longId;
    }
    public void setLongId(Long longId) {
        this.longId = longId;
    }
    public String getStrName() {
        return strName;
    }
    public void setStrName(String strName) {
        this.strName = strName;
    }
    public String getStrPhone() {
        return strPhone;
    }
    public void setStrPhone(String strPhone) {
        this.strPhone = strPhone;
    }
    public Float getFloatMortgage() {
        return floatMortgage;
    }
    public void setFloatMortgage(Float floatMortgage) {
        this.floatMortgage = floatMortgage;
    }
    public Double getDoubleBalance() {
        return doubleBalance;
    }
    public void setDoubleBalance(Double doubleBalance) {
        this.doubleBalance = doubleBalance;
    }
    public BigDecimal getBigDecimalLineOfCredit() {
        return bigDecimalLineOfCredit;
    }
    public void setBigDecimalLineOfCredit(BigDecimal bigDecimalLineOfCredit) {
        this.bigDecimalLineOfCredit = bigDecimalLineOfCredit;
    }
    public Date getDateCreateDate() {
        return dateCreateDate;
    }
    public void setDateCreateDate(Date dateCreateDate) {
        this.dateCreateDate = dateCreateDate;
    }
    public Date getDateLastLoginTime() {
        return dateLastLoginTime;
    }
    public void setDateLastLoginTime(Date dateLastLoginTime) {
        this.dateLastLoginTime = dateLastLoginTime;
    }
    public String getStrUserType() {
        return strUserType;
    }
    public void setStrUserType(String strUserType) {
        this.strUserType = strUserType;
    }
    public String getStrPsd() {
        return strPsd;
    }
    public void setStrPsd(String strPsd) {
        this.strPsd = strPsd;
    }
    public String getIgnoreField() {
        return ignoreField;
    }
    public void setIgnoreField(String ignoreField) {
        this.ignoreField = ignoreField;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SampleBO [boolIsEnable=");
        builder.append(boolIsEnable);
        builder.append(", charUserType=");
        builder.append(charUserType);
        builder.append(", byteField=");
        builder.append(byteField);
        builder.append(", shortField=");
        builder.append(shortField);
        builder.append(", intField=");
        builder.append(intField);
        builder.append(", longId=");
        builder.append(longId);
        builder.append(", strName=");
        builder.append(strName);
        builder.append(", strPhone=");
        builder.append(strPhone);
        builder.append(", floatMortgage=");
        builder.append(floatMortgage);
        builder.append(", doubleBalance=");
        builder.append(doubleBalance);
        builder.append(", bigDecimalLineOfCredit=");
        builder.append(bigDecimalLineOfCredit);
        builder.append(", dateCreateDate=");
        builder.append(dateCreateDate);
        builder.append(", dateLastLoginTime=");
        builder.append(dateLastLoginTime);
        builder.append(", strUserType=");
        builder.append(strUserType);
        builder.append(", strPsd=");
        builder.append(strPsd);
        builder.append(", ignoreField=");
        builder.append(ignoreField);
        builder.append("]");
        return builder.toString();
    }

}
