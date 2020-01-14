## 先学会用

### http

### websocket

创建boss线程组 员工线程组ServerBootstrap ChannelFuture

channelinitializer

## protobuf

isInitialized 是否所有必填字段设置值

mergefrom 另外一个消息的内容合并过来

clear() 清空字段

实现message 和message.Builder

toByteArray parseFrom  writeTo

![1556707817258](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1556707817258.png)

### netty的源码分析

nioeventloopgroup

心跳检测处理器

IdleStatehandler()

直接缓冲区 非直接缓冲区。

![1556715594210](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1556715594210.png)

用户空间，内核空间

用户空间向内核空间发出读请求，上下文切换。

内核空间读取硬盘，dma 内核空间缓冲区；

内核空间缓冲区到用户空间缓冲区

写数据

用户空间缓冲区拷贝到内核空间缓冲区。

sendfile()系统调用

![1556716833409](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1556716833409.png)

![1556717207096](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\1556717207096.png)

内存映射缓冲