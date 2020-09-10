package com.jasper.oplog.test.springboot.dao;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.jasper.oplog.test.springboot.model.Commodity;

@Component
public class CommodityDao {
    
    private static Map<Long, Commodity> db = new HashMap<>();
    static {
        Commodity commodity = new Commodity();
        commodity.setId(1L);
        commodity.setName("Surgical Mask");
        commodity.setImg("http://www.abc.org/path/to/img.jpg");
        commodity.setPriceInt(100);
        commodity.setPriceLong(100L);
        commodity.setPriceFloat(100F);
        commodity.setPriceDouble(100D);
        commodity.setPriceBigDecimal(new BigDecimal(100));
        db.put(1L, commodity);
        
        commodity = new Commodity();
        commodity.setId(2L);
        commodity.setName("hydrogen peroxide solution");
        commodity.setImg("http://www.abc.org/path2/to/img.jpg");
        commodity.setPriceInt(200);
        commodity.setPriceLong(200L);
        commodity.setPriceFloat(200F);
        commodity.setPriceDouble(200D);
        commodity.setPriceBigDecimal(new BigDecimal(200));
        db.put(2L, commodity);
    }

    public Commodity getById(Long id) {
        return db.get(id);
    }
    
    public Commodity insert(Commodity commodity) {
        db.put(commodity.getId(), commodity);
        return commodity;
    }
    
    public Boolean updatePrice(Long id, BigDecimal price) {
        Commodity commodity = db.get(id);
        commodity.setPriceInt(price.intValue());
        commodity.setPriceLong(price.longValue());
        commodity.setPriceFloat(price.floatValue());
        commodity.setPriceDouble(price.doubleValue());
        commodity.setPriceBigDecimal(price);
        db.put(id, commodity);
        return true;
    }
    
    public Integer deleteById(Long id) {
        Commodity commodity = db.remove(id);
        return commodity == null? 0: 1;
    }
}
