package com.github.djbing85.test.xml;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.djbing85.test.springboot.OpLog4jApplication;

//testng
//@ContextConfiguration(locations = {"classpath*:oplog-context-test.xml"})
//public class BaseTest extends AbstractTestNGSpringContextTests {

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {OpLog4jApplication.class})
@WebAppConfiguration
public class BaseTest {
    
    @Before
    public void init() {
//        System.out.println("begin test-----------------");
    }
 
    @After
    public void after() {
//        System.out.println("end test-----------------");
    }
    
}
