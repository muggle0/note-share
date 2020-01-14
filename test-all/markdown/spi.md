# 2019-5-15

nio多路复用 selector

dubbo 绑定端口，通道初始化，注册到选择器上 选择器监听 acccept事件

处理数据 客户端生成channel 

![1557928177267](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1557928177267.png)



![1557928226981](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1557928226981.png)

business

​		接口

rpc

​	配置层 收集配置数据

​	服务代理层 代理调用方法

​	registry 注册中心

​	cluster 路由层 负载均衡层

​	moniter 监控层 

​	protocol远程调用层

​	调用层核心  invoker protocol  exporter

remothing 远程通信层 ，架起管道 封装数据。netty框架工作在这

serialize 序列化层



