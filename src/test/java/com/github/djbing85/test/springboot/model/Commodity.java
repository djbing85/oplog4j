package com.github.djbing85.test.springboot.model;

import java.math.BigDecimal;

import com.github.djbing85.annotation.OpLogField;
import com.github.djbing85.annotation.OpLogModel;

import lombok.Data;

@Data
@OpLogModel(daoBeanId = "userDao", method = "getById")
public class Commodity {
    @OpLogField(id = 0, fieldName = "User ID")
    private Long id;
    @OpLogField(fieldName = "Name")
    private String name;
    @OpLogField(fieldName = "Image URL")
    private String img;
    @OpLogField(fieldName = "Price in Float", decimalFormat = "$#,###.##")
    private Float priceFloat;
    @OpLogField(fieldName = "Price in Double", decimalFormat = "$#,###.##")
    private Double priceDouble;
    @OpLogField(fieldName = "Price in BigDecimal", decimalFormat = "$#,###.##")
    private BigDecimal priceBigDecimal;
    @OpLogField(fieldName = "Price in Long", decimalFormat = "$#,###")
    private Long priceLong;
    @OpLogField(fieldName = "Price in Integer", decimalFormat = "$#,###")
    private Integer priceInt;
}
