## activeMQ
### 什么是jms
topic p2p
ConnectionFactory
QueueConnectionFactory
TopicConnectionFactory
Destination
死信队列
AUTO_ACKNOWLEDGE:自动确认
CLIENT_ACKNOWLEDGE:客户端确认
SESSION_TRANSACTED:事务确认,如果使用事务推荐使用该确认机制
AUTO_ACKNOWLEDGE:懒散式确认,消息偶尔不会被确认,也就是消息可能会被重复发送.但发生的概率很小
https://www.cnblogs.com/shamo89/p/8010660.html
https://blog.csdn.net/qq_32771135/article/details/79963089
https://www.jianshu.com/p/8e3de55d4373
