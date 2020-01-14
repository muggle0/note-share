## 整理一下springboot相关知识
大概过程是这样的，今天去面试，被问到springboot测试单元怎么写，我居然想不起来！就记得个@RunWith
@Test 问怎么导入xml配置文件，我头一大就记得在启动类上加注解，想半天想不起来是哪个注解了，后来才想起是@importResourece。我很气，感到自己stupid，我要整理一下我springboot的知识体系

1.测试单元怎么写（springboot2.0）？
其实用idea创建项目的时候，已经自动生成测试类了
```java
@RunWith(SpringRunner.class)
@SpringBootTest
// 类名随便取
public class MyJsmApplicationTests {
// 导入你要测试的类
    @Autowired
    TestService testService;
    @Test
    // 方法名随便取
    public void contextLoads() {
        System.out.println(testService.test());
    }

}
```
2.怎么导入配置文件
```java
SpringBootApplication
// 在启动类上加这个注解
@ImportResource("classpath:spring-aop.xml")
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
```
3.怎么自定义springmvc配置
> https://blog.csdn.net/qq_35299712/article/details/80061532?utm_source=copy

4.springboot配置aop
> https://blog.csdn.net/zknxx/article/details/53240959  

```java
@Aspect
@Component
@Slf4j
public class MyAop {
//    表达式 https://www.cnblogs.com/duenboa/p/6665474.html
    @Pointcut(value = "execution( * com.muggle.work.controller.*.*(..))")
    public void test() {

    }
    @Before(value = "test()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        System.out.println( "进入doBefore切面");
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 记录下请求内容
        log.info("URL : " + request.getRequestURL().toString());
        log.info("HTTP_METHOD : " + request.getMethod());
        log.info("IP : " + request.getRemoteAddr());
        log.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));

    }
}

```
5.@Qualifier 注解 指定注入哪个bean
> qualifier的意思是合格者，通过这个标示，表明了哪个实现类才是我们所需要的，我们修改调用代码，添加@Qualifier注解，需要注意的是@Qualifier的参数名称必须为我们之前定义@Service注解的名称之一！

6.@Order注解
> https://www.cnblogs.com/lzmrex/p/6944961.html

7.注解Spring @Configuration 和 @Component 区别
> https://blog.csdn.net/zhizhuodewo6/article/details/82020323?utm_source=blogxgwz7
https://blog.csdn.net/long476964/article/details/80626930

8.发送邮件
> https://www.cnblogs.com/ityouknow/p/6823356.html

9.整合jedis
> https://www.cnblogs.com/GodHeng/p/9301330.html

10.整合activeMQ
> https://blog.csdn.net/qiangcuo6087/article/details/79041997

注意：在windows环境下可能会启动activeMQ失败，解决办法 把conf文件下的 这俩xml 的 0.0.0.0改成127.0.0.1

11.配置logback（研究过一段时间日志框架，现在忘完了，直接上代码,直接复制粘贴就能用）
```java
<?xml version="1.0" encoding="UTF-8" ?>

<!-- 级别从高到低 OFF 、 FATAL 、 ERROR 、 WARN 、 INFO 、 DEBUG 、 TRACE 、 ALL -->
<!-- 日志输出规则 根据当前ROOT 级别，日志输出时，级别高于root默认的级别时 会输出 -->
<!-- 以下 每个配置的 filter 是过滤掉输出文件里面，会出现高级别文件，依然出现低级别的日志信息，通过filter 过滤只记录本级别的日志 -->
<!-- scan 当此属性设置为true时，配置文件如果发生改变，将会被重新加载，默认值为true。 -->
<!-- scanPeriod 设置监测配置文件是否有修改的时间间隔，如果没有给出时间单位，默认单位是毫秒。当scan为true时，此属性生效。默认的时间间隔为1分钟。 -->
<!-- debug 当此属性设置为true时，将打印出logback内部日志信息，实时查看logback运行状态。默认值为false。 -->
<configuration scan="true" scanPeriod="60 seconds" debug="false">

    <!-- 动态日志级别 -->
    <jmxConfigurator/>

    <!-- 定义日志文件 输出位置 -->
    <property name="log_dir" value="logs"/>

    <!-- 日志最大的历史 30天 -->
    <property name="maxHistory" value="30"/>

    <!-- ConsoleAppender 控制台输出日志 -->
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                <!-- 设置日志输出格式 -->
                %d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %logger - %msg%n
            </pattern>
        </encoder>
    </appender>

    <!-- ERROR级别日志 -->
    <!-- 滚动记录文件，先将日志记录到指定文件，当符合某个条件时，将日志记录到其他文件 RollingFileAppender -->
    <appender name="ERROR" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 过滤器，只记录WARN级别的日志 -->
        <!-- 果日志级别等于配置级别，过滤器会根据onMath 和 onMismatch接收或拒绝日志。 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <!-- 设置过滤级别 -->
            <level>ERROR</level>
            <!-- 用于配置符合过滤条件的操作 -->
            <onMatch>ACCEPT</onMatch>
            <!-- 用于配置不符合过滤条件的操作 -->
            <onMismatch>DENY</onMismatch>
        </filter>
        <!-- 最常用的滚动策略，它根据时间来制定滚动策略.既负责滚动也负责出发滚动 -->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!--日志输出位置 可相对、和绝对路径 -->
            <fileNamePattern>
                ${log_dir}/error/%d{yyyy-MM-dd}/shop-admin.log
            </fileNamePattern>
            <!-- 可选节点，控制保留的归档文件的最大数量，超出数量就删除旧文件假设设置每个月滚动，且<maxHistory>是6， 则只保存最近6个月的文件，删除之前的旧文件。注意，删除旧文件是，那些为了归档而创建的目录也会被删除 -->
            <maxHistory>${maxHistory}</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>
                <!-- 设置日志输出格式 -->
                %d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %logger - %msg%n
            </pattern>
        </encoder>
    </appender>


    <!-- WARN级别日志 appender -->
    <appender name="WARN" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <!-- 过滤器，只记录WARN级别的日志 -->
        <!-- 果日志级别等于配置级别，过滤器会根据onMath 和 onMismatch接收或拒绝日志。 -->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <!-- 设置过滤级别 -->
            <level>WARN</level>
            <!-- 用于配置符合过滤条件的操作 -->
            <onMatch>ACCEPT</onMatch>
            <!-- 用于配置不符合过滤条件的操作 -->
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!--日志输出位置 可相对、和绝对路径 -->
            <fileNamePattern>${log_dir}/warn/%d{yyyy-MM-dd}/shop-admin.log</fileNamePattern>
            <maxHistory>${log.maxHistory}</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %logger - %msg%n</pattern>
        </encoder>
    </appender>


    <!-- INFO级别日志 appender   -格式化输出：%d表示日期，%thread表示线程名，%-5level：级别从左显示5个字符宽度%msg：日志消息，%n是换行符-->
    <appender name="INFO" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>INFO</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${log_dir}/info/%d{yyyy-MM-dd}/shop-admin.log</fileNamePattern>
            <maxHistory>${maxHistory}</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %logger - %msg%n</pattern>
        </encoder>
    </appender>


    <!-- DEBUG级别日志 appender -->
    <appender name="DEBUG" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>DEBUG</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${log_dir}/debug/%d{yyyy-MM-dd}/shop-admin.log</fileNamePattern>
            <maxHistory>${maxHistory}</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %logger - %msg%n</pattern>
        </encoder>
    </appender>


    <!-- TRACE级别日志 appender -->
    <appender name="TRACE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>TRACE</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${log_dir}/trace/%d{yyyy-MM-dd}/shop-admin.log</fileNamePattern>
            <maxHistory>${maxHistory}</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %logger - %msg%n</pattern>
        </encoder>
    </appender>


<!--日志级别  分为OFF、FATAL、ERROR、WARN、INFO、DEBUG、ALL或者您定义的级别。Log4j建议只使用四个级别，优先级 从高到低分别是 ERROR、WARN、INFO、DEBUG。-->
    <root>
        <!-- 打印INFO级别日志及以上级别日志 -->
        <level value="INFO"/>
        <!-- 控制台输出 -->
        <appender-ref ref="console"/>
        <!-- 文件输出 -->
        <appender-ref ref="ERROR"/>
        <appender-ref ref="INFO"/>
        <appender-ref ref="WARN"/>
        <appender-ref ref="DEBUG"/>
        <appender-ref ref="TRACE"/>
    </root>
</configuration>
```

编程小技巧：当代码异常的时候 你这样：
```java
  try {

        } catch (Exception e) {
            e.printStackTrace();
        }
```
是不会被日志文件记录的，只是在控制台打印

要这样：
```java
try {

      } catch (Exception e) {
        log.error(ExceptionUtils.getStackTrace(e));

      }
```
ExceptionUtils类名全称为org.apache.commons.lang.exception.ExceptionUtils

12.执行定时任务@Schedule
> https://www.cnblogs.com/skychenjiajun/p/9057379.html?utm_source=tuicool&utm_medium=referral

13.异常统一处理 @RestControllerAdvice+ExceptionHandler(value = {Exception.class})
> https://blog.csdn.net/cl_andywin/article/details/53790510

14.做数据校验@Valid和@Validated
> https://blog.csdn.net/gaojp008/article/details/80583301

15.springboot打包问题
> https://blog.csdn.net/bluishglc/article/details/17191779
https://www.jianshu.com/p/37c6688c4fcb

16.@Async实现异步调用
> http://blog.didispace.com/springbootasync-4/

17.ApplicationContext
> https://blog.csdn.net/fubo1990/article/details/79648766
https://blog.csdn.net/yangshangwei/article/details/74937778

其他一些注解：
> https://www.cnblogs.com/tanwei81/p/6814022.html

这个大佬的博客全是干货
> http://blog.didispace.com/archives/page/13/

19.多数据源的配置
> http://blog.didispace.com/springbootmultidatasource/

spring data 注解大全：

@Qyery https://blog.csdn.net/strive_peter/article/details/76274240

 @ManyToMany(targetEntity = Role.class, cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
 > /*
     *  CascadeType. PERSIST 级联持久化 ( 保存 ) 操作
     *   CascadeType. MERGE 级联更新 ( 合并 ) 操作
     *   CascadeType. REFRESH 级联刷新操作，只会查询获取操作
     *   CascadeType. REMOVE 级联删除操作
     *   CascadeType. ALL 级联以上全部操作    ---- 默认
     * */

@Transient

@Id
    @Column(name = "id")
    @GeneratedValue(generator  = "myIdStrategy")
    @GenericGenerator(name = "myIdStrategy", strategy = SnowflakeId.TYPE)    

    @JoinTable(name = "user_role", inverseJoinColumns = {@JoinColumn(name = "role_id")}, joinColumns = {@JoinColumn(name = "user_id")}, foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
        @NotFound(action = NotFoundAction.IGNORE)

@JSONField(serialize = false)

springdata 怎么写动态sql及排序:
```java
public Result query(FnHallBusinessBean bean, Pageable pageable) {
      // 排序
      Sort.Order createSort=new Sort.Order(Sort.Direction.ASC,"createdAt");
      Sort.Order numberSort=new Sort.Order(Sort.Direction.DESC,"sort");
      // 取关联表 xxx属性的 createdAt字段为排序的依据
       Sort.Order createSort=new Sort.Order(Sort.Direction.ASC,"xxx.createdAt");
      List< Sort.Order> orders = new ArrayList<>();
      orders.add(numberSort);
      orders.add(createSort);
      Sort sort=new Sort(orders);
      BackAccountVO account = UserContext.getAccount();
      Page<FnHallBusiness> res = fnHallBusinessRepository.findAll((Root<FnHallBusiness> root, CriteriaQuery<?> query, CriteriaBuilder builder) -> {
          Predicate predicate = builder.isNull(root.get("deletedAt"));
          if (account != null) {
              predicate = builder.and(predicate, builder.equal(root.get("aaId"), account.getId()));
          }
          if (bean.getActId() != null) {
              predicate = builder.and(predicate, builder.equal(root.get("bbId"), bean.getActId()));
          }
          if (bean.getStatus() != null) {
              predicate = builder.and(predicate, builder.equal(root.get("status"), bean.getStatus()));
          }
          if (bean.getPid() != null) {
              predicate = builder.and(predicate, builder.equal(root.get("pid"), bean.getPid()));
          }
          if (StringUtils.hasText(bean.getKeyword())) {
              Predicate like = builder.like(root.get("name"), "%" + bean.getKeyword() + "%");
              like = builder.or(like, builder.like(root.get("logo"), "%" + bean.getKeyword() + "%"));
              like = builder.or(like, builder.like(root.get("url"), "%" + bean.getKeyword() + "%"));
              predicate = builder.and(predicate, like);
          }
          query.distinct(true).where(predicate);
          return query.getRestriction();
      }, new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort));
```

连接查询
```java
Page<FnHallRecord> page = repository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = root.get("deletedAt").isNull();

            Join<Object, Object> table = root.join("table", JoinType.LEFT);

            String keyword = bean.getKeyword();
            if (StringUtils.hasText(keyword)) {
                Predicate like = criteriaBuilder.like(root.get("field1"), "%" + keyword + "%");
                like = criteriaBuilder.or(like, criteriaBuilder.like(root.get("field2"), "%" + keyword + "%"));
                like = criteriaBuilder.or(like, criteriaBuilder.like(table.get("field3"), "%" + keyword + "%"));
                predicate = criteriaBuilder.and(predicate, like);
            }
      }
```
暂时想不到其他总结的地方了
