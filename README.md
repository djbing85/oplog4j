# OpLog4j

[中文版](#<https://github.com/djbing85/oplog4j/blob/master/README_zh_CN.md>)

This document introduce a common tool to generate Operation Log base on Spring: OpLog4j



# Background

A lot of project requires to record user operations, especially in bank and in government. The audit and supervise requirement request to log all changes upon a record: from create till delete.

| operation type | typical operation content |
| -------------- | ------------------------- |
| CREATE         | User create order         |
| UPDATE         | Alter delivery address    |
| UPDATE         | Use the coupon            |
| UPDATE         | Adjust price              |
| DELETE         | Archive history order     |

Such requirements are very common.

![Record-lifeCycle](https://github.com/djbing85/oplog4j/blob/master/imgs/1599011251766.png)

A typical op-log should include below elements:

​	Operate time, like *2020-09-02 09:30:00*

​	Operator,  such as *xxx@gmail.com*

​	Operate summary,  for example *new user register, transfer, new order* etc.

​	Change Detail or difference, *balance:100 --> 200*

Eventually sum up to a "change record", showing in specific web page pending for supervise and audit.

Below demonstrate a traditional System Audit Log web page

![sampleImg](https://confluence.atlassian.com/doc/files/829076528/829076529/2/1498453102809/AuditLog_820px_Annotated.png)



# OpLog4j Output

## Example

Let's first have a look at a real output generated by OpLog4j. We format the content and as well added some comments for easy understanding. Developers could refer to this example and decide whether OpLog4j could fit the project requirements.



```
//Below is the content from a JUnit test case from OpLog4j
//@See com.jasper.oplog.model.DefaultOpLog for more detail
summary: update user info	//operate summary

operator: system admin		//Here we write down the operator name; If operator is an ID, could also be converted to corresponding name easily

//UserBO before method execution, JSON string format
pre: {"auditStatus":0,"balance":815146771322186439.1516528490624142744280788974720053374767303466796875,"classifyMsg":"top secret: 06dd9897-8600-49bf-a61a-ef99dae25136","createTime":1599493177359,"ignoreField":"invisible.de272fdd-f483-4a5c-b841-3b6ab4932941","phone":"911","pswd":"The quick brown fox jumps over the lazy dog","status":0,"type":"e","userId":1,"userName":"jasper.d.0e6721ab-f88a-4144-908a-e5667792fe7a"}


//UserBO after method execution, JSON string format
post: {"auditStatus":1,"balance":5825067578147956587.33458620514737635875945898078498430550098419189453125,"classifyMsg":"top secret: 677b5c9d-b7f5-4ef3-9f67-d8b37391cbad","createTime":1599493177359,"ignoreField":"invisible.9a1c12c3-6ea9-4249-9176-9f13ddd464e9","phone":"911","pswd":"The quick brown fox jumps over the lazy dog","status":1,"type":"p","userId":1,"userName":"jasper.d.1454e20f-3f34-46d5-8c4e-cb20442348f8"}


//Difference between before and after, generates at com.jasper.oplog.aop.DefaultOpLogAOPInterceptor.getModelDiff(Class<BO>, Object, Object), developers could customize diff layout in favor by extend AbstractOpLogAOPInterceptor.
diff: 
	//<field name> <value before change> --> <value after change>
	User Name: jasper.d.6b25870e-bf31-448d-a3e2-8c19a87b915e --> jasper.d.7ea08e0e-0409-4da1-b283-e04de87a93d8
	//status's value is Integer 0 or 1, here display a converted String [Enable/Disable], refer to subsequent documents for more detail
	Status: Enable --> Disable
	//Pay attension here to the "0", it is a convert failure with a wrong fieldMapping config, result to a raw value display
	Audit Status: 0 --> PENDING
	//Compare to the original value-7500572336794443600.2980875307817212327421430018148384988307952880859375, we can see the digit was formatted
	Balance: -7,500,572,336,794,443,600.3 --> 8,830,565,855,212,825,657.58

//operate time
opTime: Tue Sep 08 11:05:01 CST 2020

//operate type
opType: UPDATE

//BO class
modelClass: class com.jasper.oplog.test.xml.model.UserBO
```

Op-log detail listed above should have full-fill most of common requirements against operation log

Summarizes the information given above, we define the key elements against op-log

## Op-Log key elements

| Elements   | Description                          |
| ---------- | ------------------------------------ |
| summary    | Brief description of the method      |
| operator   | who perform the operation            |
| pre        | BO value before the operation        |
| post       | BO value after the operation         |
| diff       | Difference between  pre and post BO  |
| opTime     | The time the operation happen        |
| opType     | Operation type, CREATE/UPDATE/DELETE |
| modelClass | BO model class                       |



I believe that many developers have implemented similar op-log functions

Don't reinvent the wheel.

Targeted to solve general audit requirements, OpLog4j provide a reusable solution for java programmer using Spring to develop and customize op-log function rapidly.



# Getting Started

A regular MVC back-end system composed of Controller, Service and Dao, some distributed system using  micro-service would have place Service layer in different JVM, but over all still MVC structure: 

![1599013482428](https://github.com/djbing85/oplog4j/blob/master/imgs/1599013482428.png)

If your project coding style comply the MVC structure, it could be very convenient to apply OpLog4j to implement op-log requirements

![1599013947158](https://github.com/djbing85/oplog4j/blob/master/imgs/1599013947158.png)

OpLog4j use AOP interceptor: @Before and @After to generate operation log

All developer needs to do is to go through some simple configuration listed below: 

| Config Item                         | Description                                                  | Reference                                                    |
| ----------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| [Bean annotation](#Bean annotation) | annotate BO Model bean's field name, field mapping rules etc. | [OpLogModel](#OpLogModel), [OpLogField](#OpLogModel)         |
| Service/Dao join point              | annotate the join point where to generate op-log             | [OpLogJoinPoint](#OpLogJoinPoint), [OpLogParam](#OpLogParam), [OpLogID](#OpLogID) |
| Spring config                       | Support config in [xml style](#xml style) or [springboot style](#springboot style); Is it complex? FYI it only need around 8 rows to complete the xml config | FYI [Spring config](#Spring config)                          |
| Handler                             | After the join point method executed, we need handler to store the op-log into DB | Create class to implement interface [IOpLogHandler](#IOpLogHandler) |



Below are the step-by-step introduction demonstrating how OpLog4j works

## Bean annotation

BO class to generate op-log is required to have and only have one ID field as Primary Key, current OpLog4j version does not support Composite primary key;

Specially, [Composite bean](#Composite bean) that could be loaded by a unique ID key could also be supported to generate op-log via OpLog4j



OpLog4j's bean annotation are **OpLogModel** and **OpLogField**, effecting respectively on class and field.

### OpLogModel

@Target on ElementType.TYPE

| attribute | remark                                                       |
| --------- | ------------------------------------------------------------ |
| daoBeanId | DAO bean ID defined in Spring                                |
| method    | for example com.xxx.dao.UserBODao.getById(Long id), the method should be: "getById" |



OpLog4j  gets the access to the DAO and "getById" method against the BO(Business Object), it is easy to get the "ID" value from the join point later, those "attribute" together makes it easy to load the BO via java reflection

### OpLogField

@Target on ElementType.FIELD

Sample:

```java
import java.math.BigDecimal;
import java.util.Date;

import com.jasper.oplog.annotation.OpLogField;
import com.jasper.oplog.annotation.OpLogModel;

//BO class annotation, noted that in "UserBODao" you must have the "getById(Long id)" function
@OpLogModel(daoBeanId = "userBODao", method = "getById")
public class UserBO {
    
    //if it was self-define constructor(s) before introduce OpLog4j, then make sure the default constructor exists, otherwise it will be exception when generate the op-log
    public UserBO() {}

    //id = 0 means the first param of the primary key, current version does not composite primary key; 
    //fieldName: human-friendly field name, focus here if you have i18n requirements
    @OpLogField(id = 0, fieldName = "User ID")
    private Long userId;
    
    //when fieldName = "" or null, will apply raw fieldName: "userName"
    @OpLogField(fieldName = "User Name")
    private String userName;
    
    //type in DB values is "p" or "e", means "Personal" or "Enterprise", fieldMapping is a JSON String, will convert "p"/"e" to a more readable "Personal"/"Enterprise".
    @OpLogField(fieldName = "User Type", fieldMapping = "{\"p\":\"Personal\", \"e\":\"Enterprise\"}")
    private String type;
    
    //isSensitive = true means this is a sensitive field, instruct OpLog4j to hide the content to **, so that even the supervisor and auditor could not view the content via op-log. @See OpLogSensitiveTypeEnum for more detail
    @OpLogField(fieldName = "Password", isSensitive = true)
    private String pswd;
    
    //Date format supports: Date/Calendar/LocalDate/LocalTime/LocalDateTime, notice that a format failure will result to an unexpected output: createTime.toString()
    @OpLogField(fieldName = "Create Time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    //the fieldMapping here convert Integer to a String
    @OpLogField(fieldName = "Status", fieldMapping = "{0:\"Disable\", 1:\"Enable\"}")
    private Integer status;
    
    @OpLogField(fieldName = "Audit Status", fieldMapping = "{1:\"PENDING\", 2:\"PASS\", 3:\"REJECT\"}")
    private Integer auditStatus;
    
    //format the currency
    @OpLogField(fieldName = "Balance", decimalFormat = "#,###.##")
    private BigDecimal balance;
    
    //fields "ignore = true" will be ignored when generate the op-log.
    @OpLogField(ignore = true)
    private String ignoreField;
    
    //getter and setter ...
    
    //我们建议非性能关键的BO都写一个toString()方法, 方便在调试代码时查看BO的值
    //We suggest to put a toString() function on non-critical BO, it's easier to view field values upon the BO when debug
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserBO [userId=");
        builder.append(userId);
        ...
        builder.append("]");
        return builder.toString();
    }
}
```

Corresponding to the sample above, below listed the configurations with more detail

| attribute     | remark                                                       | sample value                      |
| ------------- | ------------------------------------------------------------ | --------------------------------- |
| id            | Default -1: Non primary key; When annotate on PK, set:[id = 0]; Composite primary key is currently not supported | id = 0                            |
| fieldName     | Readable field name, null or empty means to get raw field name of the BO | User Name                         |
| fieldMapping  | json string, take the field value as key, convert it to the mapping value in op-log. **Notice that convert will be skip if the mapping value is null** | {"0": "disabled", "1": "enabled"} |
| ignore        | Default false, when ignore = true, the field  will be ignored | false                             |
| isSensitive   | Default false,  when isSensitive= true,  the  field value will be masked by **maskPattern** strategy,  default maskPattern = OpLogSensitiveTypeEnum.MASK_ALL. For example field value is "12345678", the masked value will be "**" | false                             |
| maskPattern   | Currently supported mask strategy are listed in [maskPattern](#maskPattern) | OpLogSensitiveTypeEnum.MASK_ALL   |
| dateFormat    | Basic date type are supported in :[dateFormat](#dateFormat). **the format string should be compatible with the field type**, otherwise will result to a date format failure: a failure format will take [fieldValue].toString() into op-log | yyyy-MM-dd HH:mm:ss               |
| decimalFormat | Digit format string, Double/Float/Long/Integer/BigDecimal are supported, will skip the digit format when "" or null | #,###.##                          |

#### dateFormat

| Supported date type     |
| ----------------------- |
| java.util.Date          |
| java.util.Calendar      |
| java.time.LocalDate     |
| java.time.LocalTime     |
| java.time.LocalDateTime |

Noted **the format string should be compatible with the field type**, otherwise will result to a date format failure: a format failure will eventually bring `[fieldValue].toString()` into op-log

#### maskPattern

| Supported mask strategy            | original value | masked value |
| ---------------------------------- | -------------- | ------------ |
| OpLogSensitiveTypeEnum.MASK_PREFIX | 12345678       | **5678       |
| OpLogSensitiveTypeEnum.MASK_SUBFIX | 12345678       | 1234**       |
| OpLogSensitiveTypeEnum.MASK_MIDDLE | 12345678       | 12\*\*78     |
| OpLogSensitiveTypeEnum.MASK_2SIDES | 12345678       | \*\*3456\*\* |
| OpLogSensitiveTypeEnum.MASK_ALL    | 12345678       | ***          |



## Joint Point Annotation



### OpLogJoinPoint

@Target on ElementType.METHOD

Current version does not support annotate same method repeatedly

| Attribute  | Remark                                                       | Sample Value              |
| ---------- | ------------------------------------------------------------ | ------------------------- |
| summary    | Method brief introduction                                    | like: "New User Register" |
| operator   | Who perform the operate, using EL expression to evaluate the parameter from the parameter list; For example: `void updatePassword(UserBO user)`, when `operator = "user.userName"`, the op-log's operator will take `user.getUserName()` out from the parameter, eventually assign the value to [DefaultOpLog](#DefaultOpLog操作日志).operator | "user.userName"           |
| modelClass | Designate the BO class associate to the annotated method, usually cope with [OpLogID](#OpLogID). FYI [Join Point Sample](#Join Point Sample) | `UserBO.class`            |
| useReturn  | If the method edited and return the instance of the `modelClass`, then set `userReturn = true` could reduce one DB read when processing @After procedure. | Default: false            |



### OpLogParam

@Target on ElementType.PARAMETER

| Attribute | Remark                                                       | Sample Value  |
| --------- | ------------------------------------------------------------ | ------------- |
| isLoaded  | `true` means to use annotated parameter as pre-BO, `false` means to load pre-BO from DB | Default false |



### OpLogID

@Target on ElementType.PARAMETER

| Attribute | Remark                                                       | Sample Value |
| --------- | ------------------------------------------------------------ | ------------ |
| order     | Reserved attribute. Composite primary key is not supported in current version, please **do not use this attribute** | default 0    |



### Join Point Sample

```java
	//summary: a brief introduction upon the method.
	//operator: who perform the operation. in this case it's 'String operator' in the parameter list
	//OpLogParam(isLoaded = true) means to use annotated parameter 'UserBO bo' as pre-BO, in order to reduce one 'DB read' when generate the op-log
    @OpLogJoinPoint(summary = "update user info", operator = "operator")
    public UserBO updateIsLoaded(@OpLogParam(isLoaded = true)UserBO bo, String operator) {
        Assert.notNull(bo);
        Assert.notNull(bo.getUserId());
        db.put(bo.getUserId(), bo);
        return bo;
    }

	//modelClass work with @OpLogID, will fetch 'UserBO' by 'id' automatically before and after method execution. Please noted that @OpLogModel annotated upon UserBO should be config correctly
	//In UserBO should have an ID field annotated by @OpLogFIeld(field name is OK to be different), and the type between the ID field and @OpLogID parameter should be the same.
    @OpLogJoinPoint(summary = "update user name", modelClass = UserBO.class)
    public void updateNameById(@OpLogID Long id, String name) {
        Assert.notNull(id);
        UserBO bo = db.get(id);
        Assert.notNull(bo);
        bo.setUserName(name);
        db.put(id, bo);
    }

	//useReturn=true means to use method's return as post-BO rather to load the post-BO from DB when processing @After procedure. 
	//Be careful in DB some table would have defined columns using 'ON UPDATE', in this case useReturn=true will result to a dirty data in the op-log
    @OpLogJoinPoint(summary = "update user balance", useReturn=true)
    public UserBO updateBalanceById(@OpLogID Long id, BigDecimal balance) {
        Assert.notNull(id);
        UserBO bo = db.get(id);
        Assert.notNull(bo);
        bo.setBalance(balance);
        db.put(id, bo);
        return bo;
    }  
```



### modelClass

Some method would have modify multiple BO, but current version OpLog4j is not yet ready to support `@Repeatable` feature on `OpLogJoinPoint`  to generate several op-log on one method

That is, one method could only generate op-log against one BO, and follow below order to choose the `modelClass` at runtime:

```
	1. Class designate by 'OpLogJoinPoint.modelClass', usually cope with @OpLogID
	2. To use return object's class when 'OpLogJoinPoint.useReturnValue = true'
	3. Parameter annotated by @OpLogParam
	4. The first parameter's class in the method which is annotated by @OpLogModel
	Anyhow the modelClass should be annotated by @OpLogModel correctly
```



## Spring config

xml and  springboot style are both supported.

*Noted that in the source code the test cases in xml-style and springboot-style uses different BO model*

### xml style

```
    <!-- aop config -->
    <aop:aspectj-autoproxy proxy-target-class="true"/>
    <!-- handler: to output the op-log, needs to implement interface: IOpLogHandler -->
    <bean id="userOpLogHandler" class="com.jasper.oplog.test.xml.aop.handler.UserOpLogHandler"/>
    <!-- handler list, same modelClass might have more than one handler -->
    <util:list id="opLogHandlers">
       <ref bean="userOpLogHandler" />
    </util:list>
    <!-- Default op-log interceptor, if you don't like it's "diff" format, try extend AbstractOpLogAOPInterceptor and implement the format you like -->
    <bean id="defaultOpLogAOPInterceptor" class="com.jasper.oplog.aop.DefaultOpLogAOPInterceptor">
        <property name="handlers" ref="opLogHandlers" />
    </bean>
    <!-- JSON format op-log interceptor. the "diff" is organized in JSON format -->
    <!-- <bean id="jsonDiffOpLogAOPInterceptor" class="com.jasper.oplog.aop.JsonDiffOpLogAOPInterceptor">
        <property name="handlers" ref="opLogHandlers" />
    </bean> -->
```

### springboot style

```java
	//handler bean, needs to implement interface: IOpLogHandler
    @Bean
    public IOpLogHandler commodityOpLogHandler() {
        CommodityOpLogHandler h = new CommodityOpLogHandler();
        return h;
    }
	//handler bean
    @Bean
    public IOpLogHandler couponOpLogHandler() {
        CouponOpLogHandler h = new CouponOpLogHandler();
        return h;
    }
	//handler bean
    @Bean
    public IOpLogHandler orderChangeOpLogHandler() {
        OrderChangeOpLogHandler h = new OrderChangeOpLogHandler();
        return h;
    }
	//handler bean
    @Bean
    public IOpLogHandler orderOpLogHandler() {
        OrderOpLogHandler h = new OrderOpLogHandler();
        return h;
    }

	//handler list
    @Bean
    public List<IOpLogHandler> opLogHandlerList() {
        List<IOpLogHandler> list = new ArrayList<>();
        list.add(commodityOpLogHandler());
        list.add(couponOpLogHandler());
        list.add(orderChangeOpLogHandler());
        list.add(orderOpLogHandler());
        return list;
    }
    
	// DefaultOpLogAOPInterceptor, inject handler list
    @Bean
    public DefaultOpLogAOPInterceptor defaultOpAOPInterceptor() {
        DefaultOpLogAOPInterceptor defaultOpAOPInterceptor = new DefaultOpLogAOPInterceptor();
        defaultOpAOPInterceptor.setHandlers(opLogHandlerList());
        return defaultOpAOPInterceptor;
    }

	//JsonDiffOpLogAOPInterceptor
    //@Bean
    //public JsonDiffOpLogAOPInterceptor jsonDiffOpAOPInterceptor() {
    //    JsonDiffOpLogAOPInterceptor jsonDiffOpAOPInterceptor = new JsonDiffOpLogAOPInterceptor();
    //    jsonDiffOpAOPInterceptor.setHandlers(opLogHandlerList());
    //    return jsonDiffOpAOPInterceptor;
    //}
```



## IOpLogHandler

IOpLogHandler is the place you get the op-log, usually we further process the op-log, then save the op-log into DB.

```java
import com.jasper.oplog.aop.handler.IOpLogHandler;
import com.jasper.oplog.model.DefaultOpLog;
import com.jasper.oplog.test.springboot.model.UserOrder;

public class OrderOpLogHandler implements IOpLogHandler<UserOrder> {

    // BO model class
    @Override
    public Class<UserOrder> getModelClass() {
        return UserOrder.class;
    }

    @Override
    public void handleDiff(DefaultOpLog<UserOrder> log) {
        System.out.println("summary: " + log.getSummary());
        System.out.println("operator: " + log.getOperator());
        System.out.println("pre: " + log.getPre());
        System.out.println("post: " + log.getPost());
        System.out.println("diff: " + log.getDiff());
        System.out.println("opTime: " + log.getOpTime());
        System.out.println("opType: " + log.getOpType());
        System.out.println("modelClass: " + log.getModelClass());
    }
}
```



### DefaultOpLogAOPInterceptor

Below is the output of a `IOpLogHandler.handleDiff`:

```
summary: Order Change

operator: null

pre: {"commodity":{"id":2,"img":"http://www.abc.org/path2/to/img.jpg","name":"hydrogen peroxide solution","priceBigDecimal":200,"priceDouble":200.0,"priceFloat":200.0,"priceInt":200,"priceLong":200},"coupon":{"discountDesc":"20.00%","discountInt":2000,"id":2,"name":"50% OFF","priceDouble":200.0},"order":{"commodityId":2,"couponId":2,"createdTime":1599492382991,"date":"2020-09-07","dateTime":"2020-09-07T23:26:22.991","orderId":2,"time":"23:26:22.991","totalPrice":200,"userId":2},"orderId":2}

post: {"commodity":{"id":2,"img":"http://www.abc.org/path2/to/img.jpg","name":"hydrogen peroxide solution","priceBigDecimal":543,"priceDouble":543.0,"priceFloat":543.0,"priceInt":543,"priceLong":543},"coupon":{"discountDesc":"6.67%","discountInt":667,"id":2,"name":"50% OFF","priceDouble":200.0},"order":{"commodityId":2,"couponId":2,"createdTime":1599492382991,"date":"2020-09-07","dateTime":"2020-09-07T23:26:22.991","orderId":2,"time":"23:26:22.991","totalPrice":1111,"userId":2},"orderId":2}

diff:     Order: 
        Total Price: 200 --> 1,111
    Commodity: 
        Price in Float: $200 --> $543
        Price in Double: $200 --> $543
        Price in BigDecimal: $200 --> $543
        Price in Long: $200 --> $543
        Price in Integer: $200 --> $543
    Coupon: 
        Discount: 20.00% --> 6.67%

opTime: Tue Sep 08 11:05:01 CST 2020

opType: UPDATE

modelClass: class com.jasper.oplog.test.springboot.model.OrderChange
```

### JsonDiffOpLogAOPInterceptor

Only 'diff' part is different compare to `DefaultOpLogAOPInterceptor`

```
summary: Order Change

operator: null

pre: {"commodity":{"id":2,"img":"http://www.abc.org/path2/to/img.jpg","name":"hydrogen peroxide solution","priceBigDecimal":200,"priceDouble":200.0,"priceFloat":200.0,"priceInt":200,"priceLong":200},"coupon":{"discountDesc":"20.00%","discountInt":2000,"id":2,"name":"50% OFF","priceDouble":200.0},"order":{"commodityId":2,"couponId":2,"createdTime":1599621329343,"date":"2020-09-09","dateTime":"2020-09-09T11:15:29.343","orderId":3,"time":"11:15:29.343","totalPrice":500,"userId":2},"orderId":2}

post: {"commodity":{"id":2,"img":"http://www.abc.org/path2/to/img.jpg","name":"hydrogen peroxide solution","priceBigDecimal":543,"priceDouble":543.0,"priceFloat":543.0,"priceInt":543,"priceLong":543},"coupon":{"discountDesc":"6.67%","discountInt":667,"id":2,"name":"50% OFF","priceDouble":200.0},"order":{"commodityId":2,"couponId":2,"createdTime":1599621329343,"date":"2020-09-09","dateTime":"2020-09-09T11:15:29.343","orderId":3,"time":"11:15:29.343","totalPrice":1111,"userId":2},"orderId":2}

diff: [{"fieldName":"Order","subModelDiffList":[{"fieldName":"Total Price","from":"\"500\"","to":"\"1,111\""}]},{"fieldName":"Commodity","subModelDiffList":[{"fieldName":"Price in Float","from":"\"$200\"","to":"\"$543\""},{"fieldName":"Price in Double","from":"\"$200\"","to":"\"$543\""},{"fieldName":"Price in BigDecimal","from":"\"$200\"","to":"\"$543\""},{"fieldName":"Price in Long","from":"\"$200\"","to":"\"$543\""},{"fieldName":"Price in Integer","from":"\"$200\"","to":"\"$543\""}]},{"fieldName":"Coupon","subModelDiffList":[{"fieldName":"Discount","from":"\"20.00%\"","to":"\"6.67%\""}]}]

opTime: Wed Sep 09 11:15:29 CST 2020

opType: UPDATE

modelClass: class com.jasper.oplog.test.springboot.model.OrderChange
```



## DefaultOpLog

Refer to above section the output of  'IOpLogHandler.handleDiff' is a `DefaultOpLog`, let's check out it's fields

| Field      | Remark                           | Related Config and Annotation                                |
| ---------- | -------------------------------- | ------------------------------------------------------------ |
| summary    | method brief introduction        | [OpLogJoinPoint](#OpLogJoinPoint).summary                    |
| operator   | operator                         | [OpLogJoinPoint](#OpLogJoinPoint).operator                   |
| pre        | BO value before method execution | [OpLogJoinPoint](#OpLogJoinPoint).modelClass refer to [modelClass](#modelClass) |
| post       | BO value after method execution  | [OpLogJoinPoint](#OpLogJoinPoint).modelClass refer to [modelClass](#modelClass) |
| diff       | differenct between pre and post  | Generated at: com.jasper.oplog.aop.AbstructOpLogAOPInterceptor.getModelDiff(Class<BO>, Object, Object); output at: [IOpLogHandler](#IOpLogHandler).handleDiff |
| opTime     | when it happen                   | java.util.Date                                               |
| opType     | operation type                   | Based on whether pre/post is null, It is divided into three categories CREATE/UPDATE/DELETE |
| modelClass | op-log BO's class                | op-log BO's class, refer to [modelClass](#modelClass)        |



# Attentions

## Customize  constructor

if the `modelClass` has customize constructor, make sure it has a default constructor.



## IP-Address

有一些项目要求记录操作者的IP地址, 这一类需求也是可以满足的, 可以把IP与操作人按一定的格式写入OpLogJoinPoint.operator所标注的参数中, 在输出日志时分离两项, 分别保存即可. 

Some project needs to mark down operator's IP address, those requirement could be fitted: combine the IP and the operator in certain format(String concat or even JSON string), then pass it into the parameter annotated by `OpLogJoinPoint.operator`, when you get the op-log at the handler, separate and parse the operator and IP, you could then save them both into DB.

```java
//service to save a order
@OpLogJoinPoint(summary = "New Order", operator = "ext")
public orderSave(UserOrder order, String ext) {
    //save
    ...
}

...

//web controller that trigger the 'orderSave'
public testOp() {
    String operator = "Admin";
    String ip = "127.0.0.1";
    
    //order change
    UserOrder order = loadOrder(1L);
    order.setTotalPrice(new BigDecimal(1234));
    //IP and operator are joint by "@@@", pass to orderSave's parameter: 'ext'
    orderSave(order, operator + "@@@" + ip);
}

...
//IOpLogHandler.handleDiff
public void handleDiff(DefaultOpLog<UserOrder> log) {
    //get the joint string, split
    String [] strArray = log.getOperator().split("@@@");
    System.out.println("operator: " + strArray[0]);
    System.out.println("ip: " + strArray[1]);
    ...
}
```

Similarly, using JSON string could pass on much more extra information to the output op-log



## Composite bean

Composite bean must be able to load by ONE ID field.

Sample below:

```java
//use lombok
@Data
@OpLogModel(daoBeanId = "orderChangeService", method = "orderDetail")
public class OrderChange {
    //Unique ID
    @OpLogField(id = 0, fieldName = "Order ID")
    private Long orderId;
	//Composite fields listed below
    //Please don't add attributes other than fieldName
    @OpLogField(fieldName = "Order")
    private UserOrder order;
    //Please don't add attributes other than fieldName
    @OpLogField(fieldName = "Commodity")
    private Commodity commodity;
    //Please don't add attributes other than fieldName
    @OpLogField(fieldName = "Coupon")
    private Coupon coupon;
}

//////////////////////////

//OrderChangeService is the service implementation class
@Service
public class OrderChangeService {
    
    ...
    
    //this method updated some fields in OrderChange's inner-bean
    @OpLogJoinPoint(summary = "Order Change", useReturn = true)
    public OrderChange orderChange(OrderChange change) {
        orderDao.updateTotalPrice(change.getOrderId(), change.getOrder().getTotalPrice());
        commodityDao.updatePrice(change.getCommodity().getId(), change.getCommodity().getPriceBigDecimal());
        couponDao.updateDiscount(change.getCoupon().getId(), change.getCoupon().getDiscountInt());
        return orderDetail(change.getOrderId());
    }

    //Get OrderChange by orderId, this is the 'getById' method upon OrderChange, to load pre-BO and post-BO when generate the op-log
    public OrderChange orderDetail(Long orderId) {
        UserOrder order = orderDao.getById(orderId);
        Coupon coupon = couponDao.getById(order.getCouponId());
        Commodity commodity = commodityDao.getById(order.getCommodityId());
        OrderChange change = new OrderChange();
        change.setOrderId(orderId);
        UserOrder order2 = new UserOrder();
        BeanUtils.copyProperties(order, order2);
        Coupon coupon2 = new Coupon();
        BeanUtils.copyProperties(coupon, coupon2);
        Commodity commodity2 = new Commodity();
        BeanUtils.copyProperties(commodity, commodity2);
        
        change.setOrder(order2);
        change.setCoupon(coupon2);
        change.setCommodity(commodity2);
        return change;
    }
}

//////////////////////////
    //Unit test
    @Test
    public void orderChange() {
        //read data from DB
        OrderChange change = orderService.orderDetail(2L);
        //modify data
        change.getOrder().setTotalPrice(new BigDecimal(1111));
        change.getCommodity().setPriceBigDecimal(new BigDecimal(543));
        change.getCoupon().setDiscountInt(667);
        //save the modification, after that OpLog4j will generate op-log
        orderService.orderChange(change);
    }

////////////////////unit test output, check the 'diff' part
summary: Order Change
operator: null
pre: {"commodity":{"id":2,"img":"http://www.abc.org/path2/to/img.jpg","name":"hydrogen peroxide solution","priceBigDecimal":200,"priceDouble":200.0,"priceFloat":200.0,"priceInt":200,"priceLong":200},"coupon":{"discountDesc":"20.00%","discountInt":2000,"id":2,"name":"50% OFF","priceDouble":200.0},"order":{"commodityId":2,"couponId":2,"createdTime":1599534380355,"date":"2020-09-08","dateTime":"2020-09-08T11:06:20.355","orderId":3,"time":"11:06:20.355","totalPrice":500,"userId":2},"orderId":2}
post: {"commodity":{"id":2,"img":"http://www.abc.org/path2/to/img.jpg","name":"hydrogen peroxide solution","priceBigDecimal":543,"priceDouble":543.0,"priceFloat":543.0,"priceInt":543,"priceLong":543},"coupon":{"discountDesc":"6.67%","discountInt":667,"id":2,"name":"50% OFF","priceDouble":200.0},"order":{"commodityId":2,"couponId":2,"createdTime":1599534380355,"date":"2020-09-08","dateTime":"2020-09-08T11:06:20.355","orderId":3,"time":"11:06:20.355","totalPrice":1111,"userId":2},"orderId":2}
diff:         Order: 
            Total Price: 500 --> 1,111
        Commodity: 
            Price in Float: $200 --> $543
            Price in Double: $200 --> $543
            Price in BigDecimal: $200 --> $543
            Price in Long: $200 --> $543
            Price in Integer: $200 --> $543
        Coupon: 
            Discount: 20.00% --> 6.67%
opTime: Tue Sep 08 11:06:20 CST 2020
opType: UPDATE
modelClass: class com.jasper.oplog.test.springboot.model.OrderChange
```

The 'diff' part at the unit test output, we could see changes on inner bean were recorded truthfully



## Collection fields

Current version OpLog4j does not yet support the change detail against Collection fields, developer who has those requirements need to compare the collection objects manually, refer to [IOpLogHandler](#IOpLogHandler)



## Internationalization

OpLog4j is OK to i18n `fieldName` and `summary`

Developers who needs i18n has extra config to do as below:

### Use [JsonDiffOpLogAOPInterceptor](#JsonDiffOpLogAOPInterceptor) in your config file



### I18n config

In the source code the junit test use a tool: OpLog4jMessageUtils, but in your project you should use your own util class for the same function.

```java
@Configuration
public class I18nConf {

    /**
     * default LocaleResolver
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.US);
        return localeResolver;
    }

    /**
     * localeInterceptor, "lang" is the parameter name
     */
    @Bean
    public WebMvcConfigurer localeInterceptor() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
                localeInterceptor.setParamName("lang");
                registry.addInterceptor(localeInterceptor);
            }
        };
    }
    
    //if your already have one ResourceBundleMessageSource, then in OpLog4jMessageUtils inject it directly, no need to define this bean repeatedly
    @Bean
    public ResourceBundleMessageSource messageSource() {
        Locale.setDefault(Locale.CHINESE);
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("i18n/messages/messages");// path and name of the resource bundle
        source.setUseCodeAsDefaultMessage(true);
        source.setDefaultEncoding("UTF-8");
        return source;
    }
    
    //Defind an i18n util
    @Bean
    public OpLog4jMessageUtils opLog4jMessageUtils() {
        return new OpLog4jMessageUtils(messageSource());
    }
}

///
///
public class OpLog4jMessageUtils {

   private static MessageSource messageSource;

   public OpLog4jMessageUtils(MessageSource messageSource) {
       OpLog4jMessageUtils.messageSource = messageSource;
   }

   public static String get(String msgKey) {
       try {
           return messageSource.getMessage(msgKey, null, LocaleContextHolder.getLocale());
       } catch (Exception e) {
           return msgKey;
       }
   }

   /** map the value by current locale, check source code 'DiffModel' for more detail */
   public static void i18nFieldName(DiffModel dm) {
       if(dm == null || StringUtils.isEmpty(dm.getFieldName())) {
           return;
       }
       dm.setFieldName(OpLog4jMessageUtils.get(dm.getFieldName()));

       if(dm.getSubModelDiffList() == null || dm.getSubModelDiffList().size() == 0) {
           return;
       }
       for(DiffModel subDm: dm.getSubModelDiffList()) {
           i18nFieldName(subDm);
       }
   }
}
```

### Config messages.properties

As in the source code: 

/oplog4j/src/test/resources/i18n/messages/messages_zh_CN.properties

```
#Chinese
coupon.diff=差异
coupon.discount=折扣
total.price=总价
```

/oplog4j/src/test/resources/i18n/messages/messages_en_US.properties

```
#English
coupon.diff=DIff
coupon.discount=Discount
total.price=Total Price
```

### Config fieldName annotated by [OpLogField](#OpLogField)

fieldName must match the keys inside messages.properties:

```
	//old fieldName
    //@OpLogField(fieldName = "Total Price", decimalFormat = "#,###.##")
    //i18n fieldName
    @OpLogField(fieldName = "total.price", decimalFormat = "#,###.##")
    private BigDecimal totalPrice;
```

### Start web server

In Eclipse, open source code: `com.jasper.oplog.test.springboot.OpLog4jApplication`, Right-click it: **Run As --> 1 Java Application**

open url: <http://127.0.0.1:8080/i18nTest?lang=en_US> 

```json
{"fieldName":"Coupon","subModelDiffList":[{"fieldName":"Discount","from":"\"20.00%\"","to":"\"6.67%\""}]}
```

open url in a new tab: <http://127.0.0.1:8080/i18nTest?lang=zh_CN

```json
{"fieldName":"Coupon","subModelDiffList":[{"fieldName":"折扣","from":"\"20.00%\"","to":"\"6.67%\""}]}
```

Can you see the difference?



# Functions to be implemented



## Difference between Collection fields



## @Repeatable feature on OpLogJoinPoint



## Composite primary key





# At the end

This project is still under maintenance, you're welcome to adopt OpLog4j Into your project, raise an Issue is very appreciate.

If this tool does help you and your project, please feel free to share it to your friends.



