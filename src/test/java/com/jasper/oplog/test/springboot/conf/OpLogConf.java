package com.jasper.oplog.test.springboot.conf;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jasper.oplog.aop.JsonDiffOpLogAOPInterceptor;
import com.jasper.oplog.aop.handler.IOpLogHandler;
import com.jasper.oplog.test.springboot.handler.CommodityOpLogHandler;
import com.jasper.oplog.test.springboot.handler.CouponOpLogHandler;
import com.jasper.oplog.test.springboot.handler.OrderChangeOpLogHandler;
import com.jasper.oplog.test.springboot.handler.OrderOpLogHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class OpLogConf {

    @Bean
    public IOpLogHandler commodityOpLogHandler() {
        CommodityOpLogHandler h = new CommodityOpLogHandler();
        return h;
    }
    @Bean
    public IOpLogHandler couponOpLogHandler() {
        CouponOpLogHandler h = new CouponOpLogHandler();
        return h;
    }
    @Bean
    public IOpLogHandler orderChangeOpLogHandler() {
        OrderChangeOpLogHandler h = new OrderChangeOpLogHandler();
        return h;
    }
    @Bean
    public IOpLogHandler orderOpLogHandler() {
        OrderOpLogHandler h = new OrderOpLogHandler();
        return h;
    }

    @Bean
    public List<IOpLogHandler> opLogHandlerList() {
        List<IOpLogHandler> list = new ArrayList<>();
        list.add(commodityOpLogHandler());
        list.add(couponOpLogHandler());
        list.add(orderChangeOpLogHandler());
        list.add(orderOpLogHandler());
        return list;
    }
    
//    @Bean
//    public DefaultOpLogAOPInterceptor defaultOpLogAOPInterceptor() {
//        DefaultOpLogAOPInterceptor defaultOpAOPInterceptor = new DefaultOpLogAOPInterceptor();
//        defaultOpAOPInterceptor.setHandlers(opLogHandlerList());
//        return defaultOpAOPInterceptor;
//    }
    
    @Bean
    public JsonDiffOpLogAOPInterceptor jsonDiffOpLogAOPInterceptor() {
        JsonDiffOpLogAOPInterceptor jsonDiffOpAOPInterceptor = new JsonDiffOpLogAOPInterceptor();
        jsonDiffOpAOPInterceptor.setHandlers(opLogHandlerList());
        return jsonDiffOpAOPInterceptor;
    }
}
