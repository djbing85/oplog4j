# OpLog4j

本文介绍一个基于Spring的通用操作日志生成工具: OpLog4j

# 背景

相信很多的项目都会有需要记录用户操作记录的需求, 特别是一些银行与政府的项目, 要把一条记录从生成到消亡中间所有变更都记录下来, 以备审查需要.

| 操作类型 | 典型的操作内容 |
| -------- | -------------- |
| 生成     | 客户新增订单   |
| 修改记录 | 修改收货地址   |
| 修改记录 | 使用优惠卷     |
| 修改记录 | 修改价格       |
| 删除     | 取消订单       |

诸如此类的审核操作日志的需要, 是广泛存在的

![Record-lifeCycle](imgs\1599011251766.png)

一个典型的记录摘要包含如下要素:

​	操作时间, 如2020-09-02 09:30:00

​	操作人,  如xxx@gmail.com

​	变更项目名称,  如新增用户, 转账, 下单等

​	变更详情, 余额:100 --> 200

最后生成一条详细的变更记录, 展示到特定的前端页面以方便管理人员审查

如下是一个传统系统审查日志的界面示例

![sampleImg](https://confluence.atlassian.com/doc/files/829076528/829076529/2/1498453102809/AuditLog_820px_Annotated.png)



# 效果

## 实例

我们先来看一个OpLog4j生成的操作日志实例. 为了便于查看, 这里对内容做了格式化, 还添加了一些注释说明.

开发者可以根据这个实例的内容判断OpLog4j是否能满足产品的需求.



```
//以下内容是基于一个单元测试中, com.jasper.oplog.model.DefaultOpLog的真实输出
summary: update user info	//操作项名称, 这里是更新用户信息

operator: system admin		//这里记录了操作人, 如果你的操作人是一个ID, 也可以在想要的地方进行转换

//变更前的UserBO, JSON格式
pre: {"auditStatus":0,"balance":815146771322186439.1516528490624142744280788974720053374767303466796875,"classifyMsg":"top secret: 06dd9897-8600-49bf-a61a-ef99dae25136","createTime":1599493177359,"ignoreField":"invisible.de272fdd-f483-4a5c-b841-3b6ab4932941","phone":"911","pswd":"The quick brown fox jumps over the lazy dog","status":0,"type":"e","userId":1,"userName":"jasper.d.0e6721ab-f88a-4144-908a-e5667792fe7a"}


//变更后的UserBO, JSON格式
post: {"auditStatus":1,"balance":5825067578147956587.33458620514737635875945898078498430550098419189453125,"classifyMsg":"top secret: 677b5c9d-b7f5-4ef3-9f67-d8b37391cbad","createTime":1599493177359,"ignoreField":"invisible.9a1c12c3-6ea9-4249-9176-9f13ddd464e9","phone":"911","pswd":"The quick brown fox jumps over the lazy dog","status":1,"type":"p","userId":1,"userName":"jasper.d.1454e20f-3f34-46d5-8c4e-cb20442348f8"}


//变更详情, 变更详情的内容是在com.jasper.oplog.aop.DefaultOpLogAOPInterceptor.getModelDiff(Class<BO>, Object, Object)中生成的, 可以通过继承类的方式定制自己想要的变更详情样式.
diff: 
	//<变更项目名称> <变更前的值> --> <变更后的值>
	User Name: jasper.d.6b25870e-bf31-448d-a3e2-8c19a87b915e --> jasper.d.7ea08e0e-0409-4da1-b283-e04de87a93d8
	//状态的原值是0/1, 这里转换成了Enable/Disable, 详细的实现请参考后续的文档
	Status: Enable --> Disable
	//注意这里的0, 是没有正确进行fieldMapping的情况下, 取的原值展示
	Audit Status: 0 --> PENDING
	//对比原值-7500572336794443600.2980875307817212327421430018148384988307952880859375, 可见数字被格式化了
	Balance: -7,500,572,336,794,443,600.3 --> 8,830,565,855,212,825,657.58

//操作的时间
opTime: Tue Sep 08 11:05:01 CST 2020

//类型
opType: UPDATE

//BO类
modelClass: class com.jasper.oplog.test.xml.model.UserBO
```

上面这样详细的操作变更详情, 应该就可以满足大部分对于操作日志的需求了.

这里我们总结一下操作日志的几个要素:

## 操作日志的要素

| 要素       | 说明                     |
| ---------- | ------------------------ |
| summary    | 方法简要描述             |
| operator   | 操作人                   |
| pre        | 变更前BO值               |
| post       | 变更后BO值               |
| diff       | 差异                     |
| opTime     | 操作时间                 |
| opType     | 操作类型, 新增/编辑/删除 |
| modelClass | 操作日志bean的类型       |



相信很多开发者都实现过类似上面的操作日志功能

Don't reinvent the wheel.

针对这一类通用的审查需求, OpLog4j为使用**spring**的JAVA开发者提供了一套可重用的解决方案, 可供开发人员方便**快速地定制**开发易扩展的操作日志功能



# 实现流程

一个常规的MVC后台通常由Controller, Service和Dao三层构成, 一些使用微服务的系统还会把Controller和Service层放在不同的JVM中运行, 但总体上看还是MVC结构的:

![1599013482428](imgs\1599013482428.png)

如果你的项目的代码遵循了这样的结构, 就可以很方便地使用OpLog4j来实现操作日志的功能

![1599013947158](imgs\1599013947158.png)

OpLog4j通过AOP拦截器的before和after方法实现生成操作日志的功能



开发者所需要做的, 仅仅是通过一些简单配置, 就可以快速实现操作日志的功能

| 配置项目              | 说明                                                         | 类                                                           |
| --------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Bean注释              | 标记BO Model bean的属性名称, 转换规则等等                    | [OpLogModel](#OpLogModel), [OpLogField](#OpLogModel)         |
| Service/Dao的切入方法 | 配置产生日志的方法, 该方法通常变更了对应B的数据              | [OpLogJoinPoint](#OpLogJoinPoint), [OpLogParam](#OpLogParam), [OpLogID](#OpLogID) |
| Spring配置            | 支持[xml方式](#xml方式)和[springboot方式](#springboot方式)配置; 如果是xml, 仅需要最少8行配置代码 | 参考[Spring配置](#Spring配置)配置                            |
| Handler类             | 方法执行完毕后, 日志如果需要写入DB, 则需要在这里实现         | 实现对应BO Model bean的[IOpLogHandler接口](#IOpLogHandler接口) |



下面我们一步步地介绍如何实现



## Bean注释

对于需要生成操作日志的BO类, OpLog4j要求必需有且仅有一个ID做为主键, 目前暂时不支持复合主键的类;

特别地, 对于特定的[组合bean](#组合bean生成操作日志), 也可以通过巧妙的配置达到生成操作日志的功能, 但这类组合bean也需要满足这样的要求: 能够由一个ID为主键从存储介质中加载.



Bean 注释的类是OpLogModel与OpLogField, 分别作用于类与属性上.

### OpLogModel

这个annotation 的作用域是ElementType.TYPE

| 属性      | 说明                                                         |
| --------- | ------------------------------------------------------------ |
| daoBeanId | Spring中定义的DAO bean ID                                    |
| method    | 如com.xxx.dao.UserBODao.getById(Long id), 则method应该取值: "getById" |

通过上面两个配置, 我们就可以从Spring中获取得到一个BO(Business Object)对应DAO的getById方法. 这个方法在生成操作日志时获取"变更前对象"和"变更后对象"起到了重要的作用.

### OpLogField

这个annotation 的作用域是ElementType.FIELD

我们直接给出一个例子:

```java
import java.math.BigDecimal;
import java.util.Date;

import com.jasper.oplog.annotation.OpLogField;
import com.jasper.oplog.annotation.OpLogModel;

//BO类注释, 注意代码中一定要有对应的com.xxx.dao.UserBODao.getById(Long id)方法
@OpLogModel(daoBeanId = "userBODao", method = "getById")
public class UserBO {
    
    //如果有自定义的构造方法, 那么请一定补充一个默认构造方法, 否则会报异常导致无法生成操作日志
    public UserBO() {}

    //id = 0表示这是主键的第0个参数, 目前只支持一个属性做主键, 复合主键功能尚不支持; 
    //fieldName是属性的名称, 如果有国际化的需求, 需要注意这里
    @OpLogField(id = 0, fieldName = "User ID")
    private Long userId;
    
    //fieldName为""或者null时, 会直接使用fieldName = "userName"
    @OpLogField(fieldName = "User Name")
    private String userName;
    
    //DB中type的取值是p/e, 分别表示Personal/Enterprise, fieldMapping则是一个JSON, 在生成对比的变化内容时, 会把p/e转换成可读性更好的Personal/Enterprise.
    @OpLogField(fieldName = "User Type", fieldMapping = "{\"p\":\"Personal\", \"e\":\"Enterprise\"}")
    private String type;
    
    //isSensitive表示这是一个敏感字段, 需要把内容转换成**, 即审查人员也是不能看到具体内容的; @See OpLogSensitiveTypeEnum 查看更多细节
    @OpLogField(fieldName = "Password", isSensitive = true)
    private String pswd;
    
    //日期格式化支持Date/Calendar/LocalDate/LocalTime/LocalDateTime, 注意格式化失败时会直接输出createTime.toString()
    @OpLogField(fieldName = "Create Time", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    //这里的fieldMapping对Integer进行了转换 
    @OpLogField(fieldName = "Status", fieldMapping = "{0:\"Disable\", 1:\"Enable\"}")
    private Integer status;
    
    @OpLogField(fieldName = "Audit Status", fieldMapping = "{1:\"PENDING\", 2:\"PASS\", 3:\"REJECT\"}")
    private Integer auditStatus;
    
    //金额的格式化 
    @OpLogField(fieldName = "Balance", decimalFormat = "#,###.##")
    private BigDecimal balance;
    
    //标注了ignore的field不会进行内容比对. 
    @OpLogField(ignore = true)
    private String ignoreField;
    
    //getter and setter ...
    
    //我们建议非性能关键的BO都写一个toString()方法, 方便在调试代码时查看BO的值
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

对应着上面的例子, 下面是更详细的配置说明

| 属性          | 说明                                                         | 例值                               |
| ------------- | ------------------------------------------------------------ | ---------------------------------- |
| id            | 默认-1:非主键. 主键上标注: [id = 0], 暂时不支持复合主键      | id = 0                             |
| fieldName     | 属性的名称                                                   | User Name                          |
| fieldMapping  | json字符串, 会把属性值做key, 转换成对应的value输出; **注意从map中取出的value为null时, 不会进行转换**. | {"0": "disabled", "1": "enabled"}  |
| ignore        | 默认false, 为true的field在对比时会被忽略                     | false                              |
| isSensitive   | 默认false,  为true时会按maskPattern的策略对内容进行隐藏, 默认maskPattern = OpLogSensitiveTypeEnum.MASK_MIDDLE, 如内容为12345678, 则处理后的内容为12**78 | false                              |
| maskPattern   | 目前支持隐藏策略为: 隐藏前缀/后缀/中间/两边/全部, 默认隐藏全部. 详情见[附表1](#附表1) | OpLogSensitiveTypeEnum.MASK_MIDDLE |
| dateFormat    | 支持基本的日期类, 详情见[附表2](#附表2). **取值要与field匹配**, 否则会导致日期格式化异常 | yyyy-MM-dd HH:mm:ss                |
| decimalFormat | 数字的格式化, 支持Double/Float/Long/Integer/BigDecimal, 为""或null时不会对数字进行格式化 | #,###.##                           |

#### 附表1

| dateFormat支持的日期类型 |
| ------------------------ |
| java.util.Date           |
| java.util.Calendar       |
| java.time.LocalDate      |
| java.time.LocalTime      |
| java.time.LocalDateTime  |

 注意dateFormat取值要与field匹配, 避免出现日期格式化失败的情况

#### 附表2

| maskPattern支持的隐藏策略          | 策略         |
| ---------------------------------- | ------------ |
| OpLogSensitiveTypeEnum.MASK_PREFIX | 隐藏前缀     |
| OpLogSensitiveTypeEnum.MASK_SUBFIX | 隐藏后缀     |
| OpLogSensitiveTypeEnum.MASK_MIDDLE | 隐藏中间     |
| OpLogSensitiveTypeEnum.MASK_2SIDES | 隐藏两边     |
| OpLogSensitiveTypeEnum.MASK_ALL    | 默认隐藏全部 |

## 切入方法



### OpLogJoinPoint

这个annotation 的作用域是ElementType.TYPE

当前版本尚不支持重复注释同一方法

| 属性       | 说明                                                         | 例值            |
| ---------- | ------------------------------------------------------------ | --------------- |
| summary    | 方法简要说明                                                 | 如: 新增用户    |
| operator   | 操作人, 使用EL表达式读取参数列表中的某个参数作为操作人, 如方法是void updatePassword(UserBO user), 当配置了operator = "user.userName"时, 操作人就会取参数中user.getUserName(), 赋值到最后生成的操作日志类[DefaultOpLog](#DefaultOpLog操作日志).operator中 | "user.userName" |
| modelClass | 指定与方法关联的BO类, 必须与[OpLogID](#OpLogID)联合使用. [示例](#示例) | UserBO.class    |
| useReturn  | 如果方法编辑并返回了操作后的BO实例, 那么应该set userReturn = true, 可以减少一次DB的读操作 | 默认false       |



### OpLogParam

这个annotation 的作用域是ElementType.PARAMETER

| 属性     | 说明                                                         | 例值      |
| -------- | ------------------------------------------------------------ | --------- |
| isLoaded | 为true时表示方法的pre-BO直接使用被标注的对象, 为false时pre-BO则需要按对象中配置的DAO bean与主键从DB中加载 | 默认false |



### OpLogID

这个annotation 的作用域是ElementType.PARAMETER

| 属性  | 说明                                               | 例值      |
| ----- | -------------------------------------------------- | --------- |
| order | 预留字段. 当前版本不支持复合主键, 请不要配置该属性 | default 0 |



### 示例

```java
	//summary描述了方法的操作摘要, 
	//operator指定了操作人, 这里会直接取参数中operator的值
	//OpLogParam(isLoaded = true) 表示pre-BO可以直接使用被标注的参数UserBO bo, 从而减少一次DB读操作
    @OpLogJoinPoint(summary = "update user info", operator = "operator")
    public UserBO updateIsLoaded(@OpLogParam(isLoaded = true)UserBO bo, String operator) {
        Assert.notNull(bo);
        Assert.notNull(bo.getUserId());
        db.put(bo.getUserId(), bo);
        return bo;
    }

	//modelClass与@OpLogID搭配使用, 在方法执行前后, 会自动读取updateNameById前后的对象, 并生成操作日志
	//注意UserBO中需要有一个id字段(名称可以不一样), 类型要与这里的Long id匹配.
    @OpLogJoinPoint(summary = "update user name", modelClass = UserBO.class)
    public void updateNameById(@OpLogID Long id, String name) {
        Assert.notNull(id);
        UserBO bo = db.get(id);
        Assert.notNull(bo);
        bo.setUserName(name);
        db.put(id, bo);
    }

	//useReturn=true表示可以直接使用方法返回的对象作为post-BO, 不需要重复读取DB. 
	//注意这里可能有DB默认更新的字段无法反馈到操作日志中, 比如mysql中定义了ON UPDATE的字段, 使用时应该注意
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



### modelClass选择机制

生产中很多代码可能在一个方法中对多个BO进行了修改, OpLog4j还不支持在一个方法中配置多个OpLogJoinPoint来生成多个BO的操作日志.

一个方法只能对一个BO产生操作日志, 运行时按以下顺序选择modelClass:

```
	1. OpLogJoinPoint.modelClass配置的类, 通常与OpLogID搭配使用
	2. OpLogJoinPoint.useReturnValue == true时尝试使用方法返回对象的class
	3. OpLogParam注释参数的class
	4. 参数中第一个类型是被OpLogModel注释类的class
```



## Spring配置

支持xml与springboot方式的配置

*注意源代码中xml与springboot的测试代码使用了不同的BO*

### xml方式

```
    <!-- aop config -->
    <aop:aspectj-autoproxy proxy-target-class="true"/>
    <!-- 输出操作日志的handler, 需要实现IOpLogHandler接口 -->
    <bean id="userOpLogHandler" class="com.jasper.oplog.test.xml.aop.handler.UserOpLogHandler"/>
    <!-- handler列表, 可以有多个 -->
    <util:list id="opLogHandlers">
       <ref bean="userOpLogHandler" />
    </util:list>
    <!-- 默认操作日志处理类. 有需要修改操作日志的diff输出时, 可以继承AbstractOpLogAOPInterceptor, 实现自己想要的输出格式 -->
    <bean id="defaultOpAOPInterceptor" class="com.jasper.oplog.aop.DefaultOpLogAOPInterceptor">
        <property name="handlers" ref="opLogHandlers" />
    </bean>
    <!-- JSON格式的操作日志处理类.和defaultOpAOPInterceptor只需要其中一个即可 -->
    <!-- <bean id="jsonDiffOpAOPInterceptor" class="com.jasper.oplog.aop.JsonDiffOpLogAOPInterceptor">
        <property name="handlers" ref="opLogHandlers" />
    </bean> -->
```

### springboot方式

```java
	//定义handler, 注意需要实现IOpLogHandler接口
    @Bean
    public IOpLogHandler commodityOpLogHandler() {
        CommodityOpLogHandler h = new CommodityOpLogHandler();
        return h;
    }
	//定义handler
    @Bean
    public IOpLogHandler couponOpLogHandler() {
        CouponOpLogHandler h = new CouponOpLogHandler();
        return h;
    }
	//定义handler
    @Bean
    public IOpLogHandler orderChangeOpLogHandler() {
        OrderChangeOpLogHandler h = new OrderChangeOpLogHandler();
        return h;
    }
	//定义handler
    @Bean
    public IOpLogHandler orderOpLogHandler() {
        OrderOpLogHandler h = new OrderOpLogHandler();
        return h;
    }

	//定义handler list
    @Bean
    public List<IOpLogHandler> opLogHandlerList() {
        List<IOpLogHandler> list = new ArrayList<>();
        list.add(commodityOpLogHandler());
        list.add(couponOpLogHandler());
        list.add(orderChangeOpLogHandler());
        list.add(orderOpLogHandler());
        return list;
    }
    
	// 定义DefaultOpLogAOPInterceptor, 注入handler list
    @Bean
    public DefaultOpLogAOPInterceptor defaultOpAOPInterceptor() {
        DefaultOpLogAOPInterceptor defaultOpAOPInterceptor = new DefaultOpLogAOPInterceptor();
        defaultOpAOPInterceptor.setHandlers(opLogHandlerList());
        return defaultOpAOPInterceptor;
    }

	//json格式的操作日志拦截器, 与DefaultOpLogAOPInterceptor只需要配置其中一个即可
    //@Bean
    //public JsonDiffOpLogAOPInterceptor jsonDiffOpAOPInterceptor() {
    //    JsonDiffOpLogAOPInterceptor jsonDiffOpAOPInterceptor = new JsonDiffOpLogAOPInterceptor();
    //    jsonDiffOpAOPInterceptor.setHandlers(opLogHandlerList());
    //    return jsonDiffOpAOPInterceptor;
    //}
```



## IOpLogHandler接口

IOpLogHandler是输出操作日志的地方, 通常可以在这里对操作日志做进一步的加工, 然后保存到DB中

```java
import com.jasper.oplog.aop.handler.IOpLogHandler;
import com.jasper.oplog.model.DefaultOpLog;
import com.jasper.oplog.test.springboot.model.UserOrder;

public class OrderOpLogHandler implements IOpLogHandler<UserOrder> {

    // 指定操作日志的class
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

这里给出一例源码中IOpLogHandler.handleDiff的输出如下:

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

这里与DefaultOpLogAOPInterceptor不同的地方在于diff

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



## DefaultOpLog操作日志



如上一小节的IOpLogHandler.handleDiff输出, 就是一个DefaultOpLog, 我们来看一下它都有哪些属性

| 字段       | 说明         | 相关配置项                                                   |
| ---------- | ------------ | ------------------------------------------------------------ |
| summary    | 方法简要描述 | [OpLogJoinPoint](#OpLogJoinPoint).summary                    |
| operator   | 操作人       | [OpLogJoinPoint](#OpLogJoinPoint).operator                   |
| pre        | 操作前值     | [OpLogJoinPoint](#OpLogJoinPoint)方法针对的BO实例, 参考[modelClass选择机制](#modelClass选择机制) |
| post       | 操作后值     | [OpLogJoinPoint](#OpLogJoinPoint)方法针对的BO实例, 参考[modelClass选择机制](#modelClass选择机制) |
| diff       | 差异         | 在com.jasper.oplog.aop.AbstructOpLogAOPInterceptor.getModelDiff(Class<BO>, Object, Object)方法中生成; 在[IOpLogHandler](#IOpLogHandler接口).handleDiff中输出 |
| opTime     | 操作时间     | java.util.Date实例                                           |
| opType     | 操作类型     | 根据pre/post是否为null, 分为CREATE/UPDATE/DELETE三大类       |
| modelClass | 操作类       | 操作日志的BO类, 参考[modelClass选择机制](#modelClass选择机制) |



# 注意事项

## 自定义constructor

如果BO有自定义constructor, 需要写一个默认constructor.



## 记录IP

有一些项目要求记录操作者的IP地址, 这一类需求也是可以满足的, 可以把IP与操作人按一定的格式写入OpLogJoinPoint.operator所标注的参数中, 在输出日志时分离两项, 分别保存即可. 

```java
//service保存方法
@OpLogJoinPoint(summary = "保存订单", operator = "ext")
public orderSave(UserOrder order, String ext) {
    //保存
    ...
}

...

//controller某个方法
public testOp() {
    String operator = "Admin";
    String ip = "127.0.0.1";
    
    //order change
    UserOrder order = loadOrder(1L);
    order.setTotalPrice(new BigDecimal(1234));
    //IP与操作人都写在保存方法的ext字段中
    orderSave(order, operator + "@@@" + ip);
}

...
//IOpLogHandler.handleDiff
public void handleDiff(DefaultOpLog<UserOrder> log) {
    String [] strArray = log.getOperator().split("@@@");
    System.out.println("operator: " + strArray[0]);
    System.out.println("ip: " + strArray[1]);
    ...
}
```

类似地, 一些其它的信息也可以通过同样的方法传递到最终的操作日志中来



## 组合bean生成操作日志

组合bean必需可以由唯一的一个ID加载, 且需要自己在handler中处理List类型的属性

示例如下:

```java
//use lombok
@Data
@OpLogModel(daoBeanId = "orderChangeService", method = "orderDetail")
public class OrderChange {
    //唯一ID
    @OpLogField(id = 0, fieldName = "Order ID")
    private Long orderId;
	//组合属性如下
    //注意不要添加除fieldName外的其它属性
    @OpLogField(fieldName = "Order")
    private UserOrder order;
    //注意不要添加除fieldName外的其它属性
    @OpLogField(fieldName = "Commodity")
    private Commodity commodity;
    //注意不要添加除fieldName外的其它属性
    @OpLogField(fieldName = "Coupon")
    private Coupon coupon;
}

//////////////////////////

//OrderChangeService是OrderChange的service实现类
@Service
public class OrderChangeService {
    
    ...
    
    //这个方法修改了OrderChange内部几个组合bean的属性
    @OpLogJoinPoint(summary = "Order Change", useReturn = true)
    public OrderChange orderChange(OrderChange change) {
        orderDao.updateTotalPrice(change.getOrderId(), change.getOrder().getTotalPrice());
        commodityDao.updatePrice(change.getCommodity().getId(), change.getCommodity().getPriceBigDecimal());
        couponDao.updateDiscount(change.getCoupon().getId(), change.getCoupon().getDiscountInt());
        return orderDetail(change.getOrderId());
    }

    //按orderId返回OrderChange, 用于生成操作日志时加载pre-BO与post-BO
    public OrderChange orderDetail(Long orderId返回) {
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
    //单元测试
    @Test
    public void orderChange() {
        //从DB中读取数据
        OrderChange change = orderService.orderDetail(2L);
        //修改数据
        change.getOrder().setTotalPrice(new BigDecimal(1111));
        change.getCommodity().setPriceBigDecimal(new BigDecimal(543));
        change.getCoupon().setDiscountInt(667);
        //保存数据, 完成后OpLog4j会生成操作日志
        orderService.orderChange(change);
    }

////////////////////测试输出 
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

在测试的输出结果中的diff部分, 我们可以看到, orderChange.order.totalPrice等等几个内部bean的变更被如实地记录了下来.



## list属性

当前版本的OpLog4j尚不支持list属性的变更, 有这部分需求的开发者需要自行比较pre/post中的list对象, 参考[IOpLogHandler接口](#IOpLogHandler接口)



## 国际化

OpLog4j支持对fieldName和summary进行国际化

需要在输出的diff中实现国际化的开发者, 需要额外进行以下代码配置:

### OpLog4j的配置中使用[JsonDiffOpLogAOPInterceptor](#JsonDiffOpLogAOPInterceptor)



### 配置国际化拦截器

源码里国际化工具类使用了OpLog4jMessageUtils, 而通常你应该使用自己的国际化工具类实现同样的功能.

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
    
    //如果你已经有一个ResourceBundleMessageSource, 那么在OpLog4jMessageUtils中直接使用即可, 不需要重复定义这个bean
    @Bean
    public ResourceBundleMessageSource messageSource() {
        Locale.setDefault(Locale.CHINESE);
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("i18n/messages/messages");// path and name of the resource bundle
        source.setUseCodeAsDefaultMessage(true);
        source.setDefaultEncoding("UTF-8");
        return source;
    }
    
    //定义一个国际化工具类
    @Bean
    public OpLog4jMessageUtils opLog4jMessageUtils() {
        return new OpLog4jMessageUtils(messageSource());
    }
}
```

### 给每个model配置对应语言的messages.properties

如源码中: 

/oplog4j/src/test/resources/i18n/messages/messages_zh_CN.properties

```
coupon.diff=差异
coupon.discount=折扣

total.price=总价
```

/oplog4j/src/test/resources/i18n/messages/messages_en_US.properties

```
coupon.diff=DIff
coupon.discount=Discount

total.price=Total Price
```

### 配置[OpLogField](#OpLogField)中的fieldName

配置成messages.properties中对应的国际化配置项, 如: 		

```
	//原来的配置
    //@OpLogField(fieldName = "Total Price", decimalFormat = "#,###.##")
    //国际化的fieldName配置
    @OpLogField(fieldName = "total.price", decimalFormat = "#,###.##")
    private BigDecimal totalPrice;
```

### 运行WEB

如在Eclipse中, 打开源码的com.jasper.oplog.test.springboot.OpLog4jApplication, 右键Run As --> 1 Java Application

访问<http://127.0.0.1:8080/i18nTest?lang=en_US> 

```json
{"fieldName":"Coupon","subModelDiffList":[{"fieldName":"Discount","from":"\"20.00%\"","to":"\"6.67%\""}]}
```

访问<http://127.0.0.1:8080/i18nTest?lang=zh_CN

```json
{"fieldName":"Coupon","subModelDiffList":[{"fieldName":"折扣","from":"\"20.00%\"","to":"\"6.67%\""}]}
```



前端再根据需求显示即可.



# 写在最后

本工具尚在完善中, 欢迎试用及提BUG. 

如果这个工具帮助到了你, 