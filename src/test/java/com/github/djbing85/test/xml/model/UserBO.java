package com.github.djbing85.test.xml.model;

import java.math.BigDecimal;
import java.util.Date;

import com.github.djbing85.annotation.OpLogField;
import com.github.djbing85.annotation.OpLogModel;

@OpLogModel(daoBeanId = "userBODao", method = "getById")
public class UserBO {
    
    //a default constructor is a must when there is a customized one
    public UserBO() {}

    @OpLogField(id = 0, fieldName = "User ID")
    private Long userId;
    @OpLogField(fieldName = "User Name")
    private String userName;
    @OpLogField(fieldName = "User Type", fieldMapping = "{\"p\":\"Personal\", \"e\":\"Enterprise\"}")
    private String type;
    @OpLogField(fieldName = "Phone No.")
    private String phone;
    @OpLogField(fieldName = "Password", isSensitive = true)
    private String pswd;
    @OpLogField(fieldName = "Create Time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @OpLogField(fieldName = "Status", fieldMapping = "{0:\"Disable\", 1:\"Enable\"}")
    private Integer status;
    @OpLogField(fieldName = "Audit Status", fieldMapping = "{1:\"PENDING\", 2:\"PASS\", 3:\"REJECT\"}")
    private Integer auditStatus;
    @OpLogField(fieldName = "Balance", decimalFormat = "#,###.##")
    private BigDecimal balance;
    /**default ignore*/
    private String classifyMsg;
    @OpLogField(ignore = true)
    private String ignoreField;
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getPswd() {
        return pswd;
    }
    public void setPswd(String pswd) {
        this.pswd = pswd;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public BigDecimal getBalance() {
        return balance;
    }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    public String getClassifyMsg() {
        return classifyMsg;
    }
    public void setClassifyMsg(String classifyMsg) {
        this.classifyMsg = classifyMsg;
    }
    public String getIgnoreField() {
        return ignoreField;
    }
    public void setIgnoreField(String ignoreField) {
        this.ignoreField = ignoreField;
    }
    public Integer getAuditStatus() {
        return auditStatus;
    }
    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserBO [userId=");
        builder.append(userId);
        builder.append(", userName=");
        builder.append(userName);
        builder.append(", type=");
        builder.append(type);
        builder.append(", phone=");
        builder.append(phone);
        builder.append(", pswd=");
        builder.append(pswd);
        builder.append(", createTime=");
        builder.append(createTime);
        builder.append(", status=");
        builder.append(status);
        builder.append(", auditStatus=");
        builder.append(auditStatus);
        builder.append(", balance=");
        builder.append(balance);
        builder.append(", classifyMsg=");
        builder.append(classifyMsg);
        builder.append(", ignoreField=");
        builder.append(ignoreField);
        builder.append("]");
        return builder.toString();
    }
    
    
}
