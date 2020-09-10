package com.jasper.oplog.test.springboot.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.util.StringUtils;

import com.jasper.oplog.annotation.OpLogField;
import com.jasper.oplog.annotation.OpLogModel;

import lombok.Data;

@Data
@OpLogModel(daoBeanId = "couponDao", method = "getById")
public class Coupon {
    @OpLogField(id = 0, fieldName = "Coupon ID")
    private Long id;
    
    @OpLogField(fieldName = "Coupon Name")
    private String name;
    
    //1 means 0.01%, 10000 means no discount
    @OpLogField(ignore = true)
    private Integer discountInt;
    
    //this is a mapping field of discountInt, which mapped the discountInt to a string like 1%
    @OpLogField(fieldName = "Discount", decimalFormat = "##.##%")
    private String discountDesc;
    
    @OpLogField(fieldName = "Price", decimalFormat = "$#,###.##")
    private Double priceDouble;
    
    public String getDiscountDesc() {
        if(!StringUtils.isEmpty(discountDesc)) {
            return discountDesc;
        }
        if(discountInt == null) {
            return null;
        }
        BigDecimal bd = new BigDecimal(discountInt).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        return bd.toString() + "%";
    }
}
