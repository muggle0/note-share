ZooKeeper本质上是一个分布式的小文件存储系统
动物园管理员

为了保证Leader选举能过得到多数的支持，所以ZooKeeper集群的数量一般为奇数。对于2n+1台server，只要有n+1台（大多数）server可用，整个系统保持可用。
ZooKeeper 是一个典型的分布式数据一致性解决方案，分布式应用程序可以基于 ZooKeeper 实现诸如数据发布/订阅、负载均衡、命名服务、分布式协调/通知、集群管理、Master 选举、分布式锁和分布式队列等功能。
jmstransactionmanager


一个ClassLoader对象，定义了由哪个ClassLoader对象来对生成的代理对象进行加载

interfaces:　　一个Interface对象的数组，表示的是我将要给我需要代理的对象提供一组什么接口，如果我提供了一组接口给它，那么这个代理对象就宣称实现了该接口(多态)，这样我就能调用这组接口中的方法了

h:　　一个InvocationHandler对象，表示的是当我这个动态代理对象在调用方法的时候，会关联到哪一个InvocationHandler对象上
复制代码

