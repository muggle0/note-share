## 什么是nacos
Nacos 支持基于 DNS 和基于 RPC 的服务发现（可以作为springcloud的注册中心）、动态配置服务（可以做配置中心）、动态 DNS 服务。

官方介绍是这样的：
> Nacos 致力于帮助您发现、配置和管理微服务。Nacos 提供了一组简单易用的特性集，帮助您实现动态服务发现、服务配置管理、服务及流量管理。
Nacos 帮助您更敏捷和容易地构建、交付和管理微服务平台。 Nacos 是构建以“服务”为中心的现代应用架构(例如微服务范式、云原生范式)的服务基础设施。

官方网址：http://nacos.io
### nacos作为注册中心
1.先在官网上下载nacos中间件 下面教程有启动步骤
> https://nacos.io/zh-cn/docs/quick-start.html

程序启动默认占用的端口是8848（珠穆朗玛峰的高度），我们可以对端口进行修改，用编辑器打开bin目录下的startup.cmd文件 添加一行代码
> set "JAVA_OPT=%JAVA_OPT% --server.port=9090

端口号就改成9090了，如图1所示：
还要在conf文件下的application.properties中添加
> server.port=9090

否则后期配置集群会报错

如果是0.3.0版本 启动后访问下面这个地址：
> http://127.0.0.1:8848/nacos/index.html

会有一个图形化界面，如图2所示：

这个配置管理项便是nacos的注册中心服务端了，下面还有一个服务管理，是nacos注册中心 图形化界面的服务端，以后做介绍。启动成功后我们就可以开始写我们的java代码了。

先新建一个springboot项目，添加如下依赖
```java
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
  <version>0.2.0.RELEASE</version>
</dependency>
```
在resource目录下加入 bootstrap.properties文件 并添加配置中心相关信息
bootstrap.properties：
```java
#服务名
spring.application.name=nacos-config-example
# 配置中心url
spring.cloud.nacos.config.server-addr=127.0.0.1:8848
```

相应的application.properties的内容写到配置中心里面，去如图3所示：
在项目启动时就会去配置中心去读取配置信息（本地的配置文件application.properties还能用，但优先级低于配置中心的配置）
如果你不想用nacos提供的控制台，nacos也提供了java开发服务端的sdk和api,我们可以用sdk开发配置中心服务端，用java代码去操作配置中心,sdk的文档可参看官方文档。
