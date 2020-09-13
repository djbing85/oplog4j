package com.github.djbing85.test.xml.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource(locations = "classpath*:oplog-context-test.xml")
public class XmlConf {

}
