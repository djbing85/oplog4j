package com.github.djbing85.test.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(scanBasePackages = {"com.github.djbing85.test"})
public class OpLog4jApplication {
    private static final Logger logger = LoggerFactory.getLogger(OpLog4jApplication.class);

    public static void main(String[] args) {
        logger.info("OpLog4jApplication test start...");
        SpringApplication.run(OpLog4jApplication.class, args);
    }
}
