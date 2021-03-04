作者: muggle

# 序

暂无

# 目录

[TOC]

# 1. MySQL

## 1.1 mysql 架构

mysql分为server层和存储引擎

### 1.1.1  server层

- 连接器：管理连接权限验证
- 查询缓存：命中缓存直接换回查询结果
- 分析器：分析语法
- 优化器：生成执行计划，选择索引
- 执行器：操作索引返回结果

### 1.1.2  存储引擎

存储引擎负责数据的存储和提取；其架构是插件式的。innodb在mysql5.5.5版本开始成为mysql默认存储引擎。

各存储引擎比对：

- InnoDB：支持事务，支持外键，InnoDB是聚集索引，数据文件是和索引绑在一起的，必须要有主键，通过主键索引效率很高。但是辅助索引需要两次查询，先查询到主键，然后再通过主键查询到数据，不支持全文索引。
- MyISAM：不支持事物，不支持外键，MyISAM是非聚集索引，数据文件是分离的，索引保存的是数据文件的指针。主键索引和辅助索引是独立的，查询效率上MyISAM要高于InnnDB，因此做读写分离的时候一般选择用InnoDB做主机，MyISAM做从机
- Memory：有比较大的缺陷使用场景很少；文件数据都存储在内存中，如果mysqld进程发生异常，重启或关闭机器这些数据都会消失。

### 1.1.3 sql的执行过程

第一步客户端连接上mysql数据库的连接器，连接器获取权限，维持管理连接；连接完成后如果你没有后续的指令这个连接就会处于空闲状态，如果太长时间不使用这个连接这个连接就会断开，这个空闲时长默认是8小时，由wait_timeout参数控制。

第二步你往mysql数据库发送了一条sql，这个时候查询缓存开始工作，看看之前有没有执行过这个sql，如果有则直接返回缓存数据到客户端，只要对表执行过更新操作缓存都会失效，因此一些很少更新的数据表可考虑使用数据库缓存，对频繁更新的表使用缓存反而弊大于利。使用缓存的方法如以下sql，通过SQL_CACHE来指定：

```sql
select  SQL_CACHE * from table where xxx=xxx
```

第三步当未命中缓存的时候，分析器开始工作；分析器判断你是select还是update还是insert，分析你的语法是否正确。

第四步优化器根据你的表的索引和sql语句决定用哪个索引，决定join的顺序。

第五步执行器执行sql，调用存储引擎的接口，扫描遍历表或者插入更新数据。

## 1.2 mysql日志

### 1.2.1 mysql日志介绍

mysql有两个重要日志——redolog和binlog(还有一个undolog不做介绍)，redolog是独属于innodb的日志，binlog则是属于server层的日志。下面介绍这两个日志有什么用：当我们更新数据库数据的时候，这两个日志文件也会被更新，记录数据库更新操作。

redolog又称作重做日志，用于记录事务操作的变化，记录的是数据修改之后的值，不管事务是否提交都会记录下来。它在数据库重启恢复的时候被使用，innodb利用这个日志恢复到数据库宕机前的状态，以此来保证数据的完整性。redolog是物理日志，记录的是某个表的数据做了哪些修改，redolog是固定大小的，也就是说后面的日志会覆盖前面的日志。

binlog又称作归档日志，它记录了对MySQL数据库执行更改的所有操作，但是不包括SELECT和SHOW这类操作。binlog是逻辑日志，记录的是某个表执行了哪些操作。binlog是追加形式的写入日志，后面的日志不会被前面的覆盖

### 1.2.2  数据更新过程

我们执行一个更新操作是这样的：读取对应的数据到内存—>更新数据—>写redolog日志—>redolog状态为prepare—>写binlog日志—>提交事务—>redolog状态为commit，数据正式写入日志文件。我们发现redolog的提交方式为“两段式提交”，这样做的目的是为了数据恢复的时候确保数据恢复的准确性，因为数据恢复是通过备份的binlog来完成的，所以要确保redolog要和binlog一致。

## 1.3 mysql的mvcc

事务隔离级别在此略过，相信大部分小伙伴都知道相关的知识了，在这里单单只介绍mysql实现事务隔离的原理——mvcc(多版本并发控制)。在学习mvcc之前我需要先介绍快照读和当前读。

### 1.3.1 快照读和当前读

快照读就是一个`select`语句，形如：

```sql
select * from table
```

在`Repeatable read`事务隔离级别下，快照读的特点是获取当前数据库的快照数据，对于所有未commit的数据都不可见，快照读不会对数据上锁。

当前读是对所读数据上悲观锁使其他当前读无法操作数据。当前读sql包括:

```java
select ... lock in share mode

select ... for update

insert

update

delete
```

其中后面三个sql都是给数据库上排他锁（X锁），而第一个sql是给数据库上共享锁（S锁）。X锁是一旦某个当前读到这个锁，其他当前读则没有对这个事务读写的权利，其他当前读会被阻塞住。而S锁是当一个当前读对某条数据上S锁，其他当前读可以对该数据也上S锁但不能上X锁，拿到S锁的当前读可以读数据不能改数据。（关于数据库悲观锁乐观锁并发章节会介绍）。

### 1.3.2 mvcc原理

innodb实现快照读和当 前读悲观锁的技术就是mvcc。innodb在插入一条数据的时候会在后面跟上两个隐藏的列，这两个列，一个保存了这个行的创建时系统版本号，一个保存的是行的删除的系统版本号。每开始一个新的事务，系统版本号就会自动递增，事务开始时刻的系统版本号会作为事务的ID。innodb更新一条数据是设置旧数据删除版本号，然后插入一条新的数据并设置创建版本号，然后删除旧的数据。那么怎么保证快照读是读取到未commit的数据呢，两个条件：

- InnoDB只查找创建版本早于当前事务版本的数据行，即，行的系统版本号小于或等于事务的系统版本号，这样可以确保事务读取的行，要么是在事务开始前已经存在的，要么是事务自身插入或者修改过的。
- 行的删除版本，要么未定义，要么大于当前事务版本号。这样可以确保事务读取到的行，在事务开始之前未被删除。
  只有符合上述两个条件的纪录，才能作为查询结果返回。

而数据库锁也是通过比对版本号来决定是否阻塞某个事物。

## 1.4 mysql 索引 

### 1.4.1  索引介绍

索引按数据结构分可分为哈希表，有序数组，搜索树，跳表：

- 哈希表适用于只有等值查询的场景
- 有序数组适用于有等值查询和范围查询的场景，但有序数组索引的更新代价很大，所以最好用于静态数据表
- 搜索树的搜索效率稳定，不会出现大幅波动，而且基于索引的顺序扫描时，也可以利用双向指针快速左右移动，效率非常高
- 跳表可以理解为优化的哈希索引

innodb使用了B+树索引模型，而且是多叉树。虽然二叉树是索引效率最高的，但是索引需要写入磁盘，如果使用二叉树磁盘io会变得很频繁。在innodb索引中分为主键索引（聚簇索引）和非主键索引（二级索引）。主键索引保存了该行数据的全部信息，二级索引保存了该行数据的主键；所以使用二级索引的时候会先查出主键值，然后回表查询出数据，而使用主键索引则不需要回表。

对二级索引而言可使用覆盖索引来优化sql，看下面两条sql

```sql
select * from table where key=1;
select id from table where key=1;
```

key是一个二级索引，第一条sql是先查询出id，然后根据id回表查询出真正的数据。而第二条查询索引后直接返回数据不需要回表。第二条sql索引key覆盖了我们的查询需求，称作覆盖索引

### 1.4.2 普通索引和唯一索引

innoDB是按数据页来读写数据的，当要读取一条数据的时候是先将本页数据全部读入内存，然后找到对应数据，而不是直接读取，每页数据的默认大小为16KB。

当一个数据页需要更新的时候，如果内存中有该数据页就直接更新，如果没有该数据页则在不影响数据一致性的前提下将；更新操作先缓存到`change buffer`中，在下次查询需要访问这个数据页的时候再写入更新操作除了查询会将`change buffer` 写入磁盘，后台线程线程也会定期将`change buffer`写入到磁盘中。对于唯一索引来说所有的更新操作都要先判断这个操作是否会违反唯一性约束，因此唯一索引的更新无法使用`change buffer` 而普通索引可以，唯一索引更新比普通索引更新多一个唯一性校验的过程。

### 1.4.3 联合索引  

两个或更多个列上的索引被称作联合索引（复合索引）。联合索引可减少索引开销，以联合索引(a,b,c)为例，建立这样的索引相当于建立了索引a、ab、abc三个索引——Mysql从左到右的使用索引中的字段，一个查询可以只使用索引中的一部份，但只能是最左侧部分，而且当最左侧字段是常量引用时，索引就十分有效，这就是**最左前缀原则**。由最左前缀原则可知，组合索引是有顺序的，那么哪个索引放在前面就比较有讲究了。对于组合索引还有一个知识点——**索引下推**，假设有组合索引（a，b，c）有如下sql:

```sql
selet * from table where a=xxx and b=xxx
```

这个sql会进行两次筛选第一次查出`a=xxx`数据 再从`a=xxx`中查出 `b=xxx` 的数据。使用索引下推和不使用索引下推的区别在于不使用索引下推会先查出`a=xxx`数据的主键然后根据查询出的主键回表查询出全行数据，再在全行数据上查出 `b=xxx` 的数据；而索引下推的执行过程是先查出`a=xxx`数据的主键，然后在这些主键上二次查询 `b=xxx` 的主键，然后回表。

索引下推的特点：

- innodb引擎的表，索引下推只能用于二级索引
- 索引下推一般可用于所查询字段不全是联合索引的字段，查询条件为多条件查询且查询条件子句字段全是联合索引。



### 1.4.4 优化器与索引

在 索引建立之后，一条语句可能会命中多个索引，这时，就会交由优化器来选择合适的索引。优化器选择索引的目的，是找到一个最优的执行方案，并用最小的代价去执行语句。那么优化器是怎么去确定索引的呢？优化器会优先选择扫描行数最少的索引，同时还会结合是否使用临时表、是否排序等因素进行综合判断。MySQL 在开始执行sql之前，并不知道满足这个条件的记录有多少条，而只能根据mysql的统计信息来估计，而统计信息是通过数据采样得出来的。

### 1.4.5 其他索引知识点

有时候需要索引很长的字符列，这会让索引变得很大很慢还占内存。通常可以以开始的部分字符作为索引，这就是**前缀索引**。这样可以大大节约索引空间，从而提高索引效率，但这样也会降低索引的选择性。

**脏页**对数据库的影响：

当内存数据页和磁盘的数据不一致的时候我们称这个内存页为脏页，内存数据写入磁盘后数据一致，称为干净页。当要读入数据而数据库没有内存的时候，这个时候需要淘汰内存中的数据页——干净页可以直接淘汰掉，而脏页需要先刷入磁盘再淘汰。如果一个查询要淘汰的脏页太多会导致查询的时间变长。为了减少脏页对数据库性能影响，innodb会控制脏页的比例和脏页刷新时机。

## 1.5 mysql语法分析及优化

### 1.5.1 count(*)

`count(*)`对innodb而言，它需要把数据从磁盘中读取出来然后累计计数；而MyISAM引擎把一个表的总行数存在了磁盘上，所以执行`count(*)`会直接返回这个数，如果有where条件则和innodb一样。
那么如何优化`count(*)`？一个思路是使用缓存，但是需要注意双写一致的问题（双写一致性后文缓存章节会做介绍）。还可以专门设计一张表用以存储`count(*)`。

对于count(主键id)来说，InnoDB引擎会遍历整张表，把每一行的id值都取出来，返回给server 层。server层拿到id后，判断是不可能为空的，就按行累加。 对于count(1)来说，InnoDB引擎遍历整张表，但不取值。server层对于返回的每一行，放一个 数字“1”进去，判断是不可能为空的，按行累加。 单看这两个用法的差别的话，你能对比出来，count(1)执行得要比count(主键id)快。因为从引擎 返回id会涉及到解析数据行，以及拷贝字段值的操作。 对于count(字段)来说： 如果这个“字段”是定义为not null的话，一行行地从记录里面读出这个字段，判断不能为 null，按行累加； 如果这个“字段”定义允许为null，那么执行的时候，判断到有可能是null，还要把值取出来再 判断一下，不是null才累加。  而对于count(*)来说，并不会把全部字段取出来，而是专门做了优化，不取值，按行累加。所以排序效率：
> count(*)=count(1)>count(id)>count(字段)


### 1.5.2 order by

Mysql会给每个线程分配一块内存用于做排序处理，称为`sort_buffer`,一个包含排序的sql执行过程为：申请排序内存`sort_buffer`,然后一条条查询出整行数据，然后将需要的字段数据放入到排序内存中，染回对排序内存中的数据做一个快速排序，然后返回到客户端。当数据量过大，排序内存盛不下的时候就会利用磁盘临时文件来辅助排序。当我们排序内存盛不下数据的时候，mysql会使用`rowid`排序来优化。rowid排序相对于全字段排序，不会把所有字段都放入sort_buffer，所以在sort buffer中进行排序之后还得回表查询。在少数情况下，可以使用联合索引+索引覆盖的方式来优化order by。

### 1.5.3 join
在了解`join`之前我们应该先了解**驱动表**这个概念——当两表发生关联的时候就会有驱动表和被驱动表之分，驱动表也叫外表（R表），被驱动表也叫做内表（S表）。一般我们将小表当做驱动表（指定了联接条件时，满足查询条件的记录行数少的表为「驱动表」,未指定联接条件时，行数少的表为「驱动表」；MySQL 内部优化器也是这么做的）。

假设有这样一句sql（xxx 为索引）:
```sql
select * from table1 left join tablet2 on table1.xxx=table2.xxx 
```
这条语句执行过程是先遍历表table1，然后根据从表table1中取出的每行数据中的xxx值，去表table2中查找满足条件的 记录。这个过程就跟我们写程序时的嵌套查询类似，并且能够用上被驱动表的索引，这种查询方式叫`NLJ`。当xxx不是索引的时候，再使用`NLJ`的话就会对table2做多次的全表扫描（每从table1取一条数据就全表扫描一次table2），扫描数暴涨。这个时候mysql会采用另外一个查询策略。Mysql会先把table1的数据读入到一个`join_buffer`的内存空间里面去，然后
依次取出table2的每一行数据，跟`join_buffer`中的数据做对比，满足join条件的作为结果集的一部分返回。
我们在使用`join`的时候,要遵循以下几点：
- 小表驱动大表。
- 被驱动表走索引的情况下（走`NLJ`查询方式）的时候才考虑用join


### 1.5.4 sql的优化 
1） 在mysql中，如果对字段做了函数计算，就用不上索引了
如以下sql(data 为索引):
```sql
select *  from tradelog where month(data)=1;
```
优化器对这样的sql会放弃走搜索树，因为它无法知道data的区间。

2）隐式的类型转换会导致索引失效。
如以下sql:
```sql
select * from table where xxx=110717;
```
其中xxx为`varchar`型，在mysql中，字符串和数字做比较的话，将字符串转换成数字再进行比较，这里相当于使用了`CAST(xxx AS signed )` 导致无法走索引。

3）索引列参与了计算不会走索引

4）like %xxx 不会走索引，like xxx% 会走索引

5）在where子句中使用or，在innodb中不会走索引，而MyISAM会。


## 1.6执行计划和慢查询日志
### 1.6.1 执行计划
在查询sql之前加上`explain`可查看该条sql的执行计划,如：
```sql
EXPLAIN SELECT * FROM table
```
这条sql会返回这样一个表：

| id   | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | extra |      |
| ---- | ----------- | ----- | ---------- | ---- | ------------- | ---- | ------- | ---- | ---- | -------- | ----- | ---- |
| 1    | simple      |       |            |      |               |      |         |      |      |          |       |      |


这个表便是sql的执行计划，我们可以通过分析这个执行计划来知道我们sql的运行情况。现对各列进行解释：

1）id：查询中执行select子句或操作表的顺序。

2）select_type：查询中每个select子句的类型（简单 到复杂）包括：
- SIMPLE：查询中不包含子查询或者UNION；
- PRIMARY：查询中包含复杂的子部分；
- SUBQUERY：在SELECT或WHERE列表中包含了子查询，该子查询被标记为SUBQUERY；
- DERIVED：衍生，在FROM列表中包含的子查询被标记为DERIVED；
- UNION：若第二个SELECT出现在UNION之后，则被标记为UNION；
- UNION  RESULT：从UNION表获取结果的SELECT被标记为UNION RESULT；


3） type：表示MySQL在表中找到所需行的方式，又称“访问类型”，包括：
- ALL：Full Table Scan， MySQL将遍历全表以找到匹配的行；
- index：Full Index Scan，index与ALL区别为index类型只遍历索引树；
- range：索引范围扫描，对索引的扫描开始于某一点，返回匹配值域的行，常见于between  <  >  等查询；
- ref：非唯一性索引扫描，返回匹配某个单独值的所有行。常见于使用非唯一索引即唯一索引的非唯一前缀进行的查找；
- eq_ref：唯一性索引扫描，对于每个索引键，表中只有一条记录与之匹配。常见于主键或唯一索引扫描；
- onst 和 system：当MySQL对查询某部分进行优化，并转换为一个常量时，使用这些类型访问。如将主键置于where列表中，MySQL就能将该查询转换为一个常量，system是const类型的特例，当查询的表只有一行的情况下， 使用system；
- NULL：MySQL在优化过程中分解语句，执行时甚至不用访问表或索引。

4）possible_keys： 指出MySQL能使用哪个索引在表中找到行，查询涉及到的字段上若存在索引，则该索引将被列出，但不一定被查询使用。

5）key：显示MySQL在查询中实际使用的索引，若没有使用索引，显示为NULL。

6）key_len：表示索引中使用的字节数，可通过该列计算查询中使用的索引的长度。

7）ref： 表示上述表的连接匹配条件，即哪些列或常量被用于查找索引列上的值。

8）rows： 表示上述表的连接匹配条件，即哪些列或常量被用于查找索引列上的值。

9）Extra：其他重要信息 包括：
- Using index：该值表示相应的select操作中使用了覆盖索引 ；
- Using where：MySQL将用where子句来过滤结果集；
- Using temporary：表示MySQL需要使用临时表来存储结果集，常见于排序和分组查询；
- Using filesort：MySQL中无法利用索引完成的排序操作称为“文件排序”。

### 1.6.2 慢查询日志
mysql支持慢查询日志功能——mysql会将查询时间过长的sql相关信息写入日志。这个查询时间阀值由参数`long_query_time`指定，`long_query_time`的默认值为10，运行10S以上的查询sql会被记录到慢查询日志中。默认情况下，Mysql数据库并不启动慢查询日志，需要我们手动来设置这个参数。慢查询日志支持将日志记录写入文件，也支持将日志记录写入数据库表。

可通过以下sql查看慢查询日志是否开启：
```sql
show variables  like '%slow_query_log%';
```
通过以下sql开启慢查询：
```sql
set global slow_query_log=1;
```
使用sql修改慢查询日志设置只对当前数据库生效，如果MySQL重启后则会失效。如果要永久生效，就必须修改配置文件my.cnf。

通过以下sql查看修改慢查询的阈值：
```sql
show variables like 'long_query_time%';
set global long_query_time=4;
```

## 1.7主从备份

### 1.7.1主从备份原理

主从复制是指一台服务器充当主数据库服务器，另一台或多台服务器充当从数据库服务器，主服务器中的数据自动复制到从服务器之中。通过这种手段我们可以做到读写分离，主库写数据，从库读数据，从而提高数据库的可用。
MySQL主从复制涉及到三个线程，一个运行在主节点（log dump thread），其余两个(I/O thread, SQL thread)运行在从节点。

主节点 binary log dump 线程：
当从节点连接主节点时，主节点会创建一个`log dump` 线程，用于发送`binlog`的内容。在读取`binlog`中的操作时，此线程会对主节点上的`binlog`加锁，当读取完成，甚至在发动给从节点之前，锁会被释放。

 从节点I/O线程:
 用于从库将主库的`binlog`复制到本地的`relay log`中，首先，从库库会先启动一个工作线程，称为IO工作线程，负责和主库建立一个普通的客户端连接。如果该进程追赶上了主库，它将进入睡眠状态，直到主库有新的事件产生通知它，他才会被唤醒，将接收到的事件记录到`relay log`(中继日志)中。

 从节点SQL线程:
 SQL线程负责读取`relay log`中的内容，解析成具体的操作并执行，最终保证主从数据的一致性。

 ### 1.7.2 主从备份延迟
主备延迟最直接的表现是，备库消费中继日志（`relay log`）的速度，比主库生产`binlog` 的速度要慢。可能导致的原因有：
- 大事务，主库上必须等事务执行完成才会写入binlog，再传给备库，当一个事物用时很久的时候，在从库上会因为这个事物的执行产生延迟。
- 从库压力大。

主备延迟当然是不好的，那么有哪些办法尽量减小主备延迟呢？有下面几个办法：
- 一主多从——多接几个从库，让这些从库来分担读的压力。这样方法适用于从库读压力大的时候。
- 通过binlog输出到外部系统，比如Hadoop这类系统，让外部系统提供统计类查询的能力

### 1.7.3 主从备份配置
主机：
```shell

vi /etc/my.cnf


#主数据库端ID号
server_id = 1           
 #开启二进制日志                  
log-bin = mysql-bin    
#需要复制的数据库名，如果复制多个数据库，重复设置这个选项即可                  
binlog-do-db = db        
#将从服务器从主服务器收到的更新记入到从服务器自己的二进制日志文件中                 
log-slave-updates                        
#控制binlog的写入频率。每执行多少次事务写入一次(这个参数性能消耗很大，但可减小MySQL崩溃造成的损失) 
sync_binlog = 1                    
#这个参数一般用在主主同步中，用来错开自增值, 防止键值冲突
auto_increment_offset = 1           
#这个参数一般用在主主同步中，用来错开自增值, 防止键值冲突
auto_increment_increment = 1            
#二进制日志自动删除的天数，默认值为0,表示“没有自动删除”，启动时和二进制日志循环时可能删除  
expire_logs_days = 7                    
#将函数复制到slave  
log_bin_trust_function_creators = 1       
```

登录mysql
```sql
#创建slave账号account，密码123456
mysql>grant replication slave on *.* to 'account'@'10.10.20.116' identified by '123456';
#更新数据库权限
mysql>flush privileges;

# 检查log-bin
show variables like ‘log_bin’

#查看主服务器状态
show master status\G;


```

从机：
```shell
vi /etc/my.cnf


server_id = 2
log-bin = mysql-bin
log-slave-updates
sync_binlog = 0
#log buffer将每秒一次地写入log file中，并且log file的flush(刷到磁盘)操作同时进行。该模式下在事务提交的时候，不会主动触发写入磁盘的操作
innodb_flush_log_at_trx_commit = 0        
#指定slave要复制哪个库
replicate-do-db = db         
#MySQL主从复制的时候，当Master和Slave之间的网络中断，但是Master和Slave无法察觉的情况下（比如防火墙或者路由问题）。Slave会等待slave_net_timeout设置的秒数后，才能认为网络出现故障，然后才会重连并且追赶这段时间主库的数据
slave-net-timeout = 60                    
log_bin_trust_function_creators = 1

```
登录mysql
```sql
#执行同步命令，设置主服务器ip，同步账号密码，同步位置
mysql>change master to master_host='10.10.20.111',master_user='account',master_password='123456',master_log_file='mysql-bin.000033',master_log_pos=337523;
#开启同步功能
mysql>start slave;
#查看从服务器状态
mysql>show slave status\G;
```


## 1.8 分布式事务
由于篇幅问题，这里不再对分布式事物的概念做普及，直接介绍两种分布式事务: XA 分布式事务和 TCC分布式事务。

### 1.8.1 XA分布式事务

XA是两阶段提交的强一致性事物。在MySQL 5.7.7版本中，Oracle 官方将MySQL XA 一直存在的一个“bug” 进行了修复，使得MySQL XA 的实现符合了分布式事务的标准。

XA事务中的角色：
- 资源管理器（resource manager）：用来管理系统资源，是通向事务资源的途径。数据库就是一种资源管理器。资源管理还应该具有管理事务提交或回滚的能力。
- 事务管理器（transaction manager）：事务管理器是分布式事务的核心管理者。事务管理器与每个资源管理器（resource 
manager）进行通信，协调并完成事务的处理。事务的各个分支由唯一命名进行标识。

XA规范的基础是两阶段提交协议：

在第一阶段，交易中间件请求所有相关数据库准备提交（预提交）各自的事务分支，以确认是否所有相关数据库都可以提交各自的事务分支。当某一数据库收到预提交后，如果可以提交属于自己的事务分支，则将自己在该事务分支中所做的操作固定记录下来，并给交易中间件一个同意提交的应答，此时数据库将不能再在该事务分支中加入任何操作，但此时数据库并没有真正提交该事务，数据库对共享资源的操作还未释放（处于锁定状态）。如果由于某种原因数据库无法提交属于自己的事务分支，它将回滚自己的所有操作，释放对共享资源上的锁，并返回给交易中间件失败应答。

在第二阶段，交易中间件审查所有数据库返回的预提交结果，如所有数据库都可以提交，交易中间件将要求所有数据库做正式提交，这样该全局事务被提交。而如果有任一数据库预提交返回失败，交易中间件将要求所有其它数据库回滚其操作，这样该全局事务被回滚。

mysql允许多个数据库实例参与一个全局的事务。MySQL XA 的命令集合如下：
```sql
-- 开启一个事务，并将事务置于ACTIVE状态，此后执行的SQL语句都将置于该是事务中。

XA START xid
-- 将事务置于IDLE状态，表示事务内的SQL操作完成。
XA END xid

-- 事务提交的准备动作，事务状态置于PREPARED状态。事务如果无法完成提交前的准备操作，该语句会执行失败。
XA PREPARE xid

-- 事务最终提交，完成持久化。
XA COMMIT xid

-- 事务回滚终止
XA ROLLBACK xid

-- 查看MySQL中存在的PREPARED状态的xa事务。
XA RECOVER
```
MySQL 在XA事务中扮演的是参与者的角色，被事务协调器所支配。XA事务比普通本地事务多了一个`PREPARE`状态，普通事务是 begin-> commit 而分布式事务是 begin->PREPARE 等其他数据库事务都到PREPARE状态的时候再 PREPARE->commit。分布式事务sql示例：

```sql
 xa start 'aaa';
 insert into table(xxx) values(xxx);
 xa end 'aaa';
 xa prepare 'aaa';
 xa commit 'aaa';
```
XA事务存在的问题：

- 单点问题:事务管理器在整个流程中扮演的角色很关键，如果其宕机，比如在第一阶段已经完成，在第二阶段正准备提交的时候事务管理器宕机，资源管理器就会一直阻塞，导致数据库无法使用。
- 同步阻塞:在准备就绪之后，资源管理器中的资源一直处于阻塞状态，直到提交完成才能释放资源。
- 数据不一致:两阶段提交协议虽然为分布式数据强一致性所设计，但仍然存在数据不一致性的可能，比如在第二阶段中，假设协调者发出了事务commit的通知，但是因为网络问题该通知仅被一部分参与者所收到并执行了commit操作，其余的参与者则因为没有收到通知一直处于阻塞状态，这时候就产生了数据的不一致性。


### 1.8.2 TCC分布式事务
TCC又被称作柔性事务，通过事务补偿机制来达到事务的最终一致性，它不是强一致性的事务。TCC将事务分为两个阶段，或者说是由两个事务组成的。相对于XA事务来说TCC的并发性更好，XA是全局性的事务，而TCC是由两个本地事务组成。

假设我们购买一件商品，后台需要操作两张表——积分表加积分而库存表扣库存，这两张表存在于两个数据库中，使用TCC事务执行这一事务：

1）TCC实现阶段一：Try
在try阶段并不是直接减库存加积分，而是将相关数据改变为预备的状态。库存表先锁定一个库存，锁定的方式可以预留一个锁定字段，当这个字段为一的时候表示这个商品被锁定。积分表加一个数据，这个数据也是被锁定状态，锁定方式和库存表一样。其sql形如：
```sql
update stock set lock=1 where id=1;
insert into credits (lock,...) values (1,...)

```
这两条sql如果都执行成功则进入 Confirm阶段，如果执行不成功则进入Cancel阶段

2）TCC实现阶段二：Confirm

这一阶段正式减库存加积分订单状态改为已支付。执行sql将锁定的库存扣除，为累加积分累加，以及一些其他的逻辑。

3）TCC实现阶段三：Cancel
当try阶段执行不成功，就会执行这一阶段，这个阶段将锁定的库存还原，锁定的积分删除掉。退回到事务执行前的状态。

TCC事务原理很简单，使用起来却不简单。首先TCC事务对系统侵入性很大，其次是让业务逻辑变得复杂。在实际使用中我们必须依赖TCC事务中间件才能让TCC事务得以实现。通常一个TCC事务实现大概是这样子的：某个服务向外暴露了一个服务，这个服务对外正常调用，其他服务并不能感知到TCC事务的存在，而其服务内部，分别实现了Try,Confirm,Cancel三个接口，注册到TCC中间件上去。当调用这个服务的时候，其事务操作由该服务和TCC中间件共同完成。

而TCC事务中间件还要做好其他事情，比如确保Confirm或者Cancel执行成功，如果发现某个服务的Cancel或者Confirm一直没成功，会不停的重试调用他的Cancel或者Confirm逻辑，务必要他成功！即使在尝试多次后无法成功也能通知到系统需要人工排查异常。TCC事务还要考虑一些异常情况的处理，比如说订单服务突然挂了，然后再次重启，TCC分布式事务框架要能够保证之前没执行完的分布式事务继续执行。TCC分布式事务框架还需要做好日志的记录，保存下来分布式事务运行的各个阶段和状态，以便系统上线后能够排查异常，恢复数据。目前开源的TCC事务框架有：`Seata` `ByteTCC` `tcc-transaction` 等。

## 1.9 sql优化与语法

### 1.9.1 sql优化问题

- 反范式的设计 合理反范式的设计可以提高查询效率
- 辅助表的设计 缓存表（设计冗余数据提高查询效率）/汇总表（统计数据作为一行数据）/计数器表（一个数据行作为一个计数器） 
- 物化视图 
- 其他常见sql优化的方式

### 1.9.2 mysql 语法

常用：

```
 # 查询所有的字段
 show full cloumns from table
 
 # 查询锁 当事务正在执行时会显示sql
 
 show processlist
 
 show OPEN TABLES where In_use > 0;

# 查看正在锁的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCKS; 
 # 查看等待锁的事务
   SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCK_WAITS;
 
 # 字段增删改
 alter table [modify,add ,drop ,rename,alter]
 
 # 删除表数据
 truncate 
 
```

视图 ：

```sql
# Mysql视图的定义在from关键字后面不能包含子查询
create view xx as select ...

# 修改视图
alter view xx as select ...
# 查看视图
show create view xx;
#删除视图　可删除多个
drop view xx,xxx; 

# 重命名
Rename table 视图名 to 新视图名;
```
视图只保存了sql逻辑不保存查询结果，如果想从根本上优化视图，采用物化视图的技术。mysql要借助第三方的中间件才能实现该功能

分区表：

分区表是将一个表拆成多个区间，以减少查询时的索引范围，分区方式包括

- range 范围分区

- list 类似range 不连续

- hash 对用户的表达式所返回的返回值进行分区

- key 类似hash 多列

```sql
# 范围分区
create table test (
        id int 
) partition by range(id)(
        partition  p0 values less than(5)
)
```
存储过程，存储过程参数包含 in out inout 三种：

```sql
# 调用
call xxx（实参列表）

delimiter $
create procedure test() begin
sql
end $
delimiter ;

# in 模式存储过程
...
create procedure test(in xxx varchar(50) )
...

 out 模式存储过程

create procedure test(out xxx varchar(50))
# 删除 
drop procedure xxx; 
#查看 
show create procedure xxx
```
变量 包含系统变量 用户变量 服务器每次启动全局变量都会被赋初始值，
会话变量在每次建立一个新的连接的时候，由MYSQL来初始化。MYSQL会将当前所有全局变量的值复制一份。来做为会话变量

```
# 系统变量（全局变量和会话变量）查看变量

show SESSION VARIABLES; 会话变量

show global VARIABLES

show global variavles like ''

## 设置变量

select @@global.xxx;

set global.xxx=xxxx

set @@global.autocommit=1;

```

```
# 自定义变量 （用户变量和 局部变量） 用户变量只作用于当前会话,可以声明在 begin 外部.

#声明并初始化
set @xxx=xxx;
set @xxx:=xxx;
select @xxx:=xxx;

# 使用
select count(*) into @count from table;
set @name='xx';
# 弱类型特性
set @name=11;

# 查看
select @count
```

```
# 局部变量只作用于begin end 且必须是第一句话
# 声明 
# 强类型
declare 变量名 类型;
declare 变量名 类型 值

# 赋值 
select count(*) into @count from table

# 使用 
select xxx;

```

触发器：
```
# 创建触发器
CREATE TRIGGER trigger_name trigger_time trigger_event ON tb_name FOR EACH ROW trigger_stmt

```
trigger_name：触发器的名称

tirgger_time：触发时机，为BEFORE或者AFTER

trigger_event：触发事件，为INSERT、DELETE或者UPDATE

tb_name：表示建立触发器的表明，就是在哪张表上建立触发器

trigger_stmt：触发器的程序体，可以是一条SQL语句或者是用BEGIN和END包含的多条语句

MySQL有以下六种触发器：BEFORE INSERT,BEFORE DELETE,BEFORE UPDATE
AFTER INSERT,AFTER DELETE,AFTER UPDATE

```
# 查看触发器
SHOW TRIGGERS

SELECT * FROM information_schema.triggers WHERE TRIGGER_NAME='xxx';

# 删除触发器

 drop trigger trigger_name;
 
```

游标：

```SQL
 DECLARE 光标名称 CURSOR FOR 查询语法
```
- 不敏感：数据库可以选择不复制结果集
- 只读
- 不滚动：游标只能向一方向前进，并且不可以跳过任何一行数据
- 游标是针对行操作的，对从数据库中 select 查询得到的结果集的 每一行可以
- 性能不高
- 只能一行一行操作
- 使用游标会产生死锁，造成内存开销大


事件：

```
SHOW VARIABLES LIKE 'event_scheduler';
#开启事件，如果想要每次重启数据库服务后，事件仍然生效需要在mysql.ini文件中加入event_scheduler = ON; 的语句
SET GLOBAL event_scheduler = ON;
#查询数据库所有创建的事件
SELECT * FROM mysql.event;
#启用事件任务
ALTER EVENT event1 ENABLE;
#禁用事件任务
ALTER EVENT event1 DISABLE;
#查看事件的定义
SHOW CREATE EVENT event1;

DELIMITER ;;
CREATE EVENT `event1` ON SCHEDULE EVERY 3 SECOND ON COMPLETION PRESERVE DISABLE COMMENT ''
DO 
BEGIN
xxx
END
;;
DELIMITER ;

```

### 1.9.3 mysql 面试问题

主从复制的binlog格式 

- Statement：基于SQL语句级别的Binlog，每条修改数据的SQL都会保存到Binlog里面。
- ROW：基于行级别，每一行数据的变化都会记录到Binlog里面，但是并不记住原始SQL语句，因此它会记录的非常详细，日志量也比statement格式记录的多得多。在主从复制中，这样的Binlog格式不会因存储过程或触发器原因造成主从数据不一致的问题。

- Mixed：混合Statement和Row模式。

主从复制的同步方式

- 异步：主库不去管从库是否同步成功
- 同步： 只要一个从库接收到日志并写入relaylog就算成功
- 半同步：当主库执行完一个事务所有的从库都同步完才返回

mysql 基于GTID复制的优缺点

GTID即全局事务ID，其保证为每一个在主上提交的事务在复制集群中可以生成一个唯一的ID

优点：可以很方便的进行故障转移，从库不会丢失主库上的任何修改

缺点：故障处理比较复杂，对执行的SQL有一定的限制

配置示例：

```
server_id= 1 # 服务器id
log-bin=xxx # binlog的文件名
gtid_mode=on # gitd 模式

binlog_format= row
```

innodb 和myisam的区别及主从复制的混用

-  InnoDB支持事务，MyISAM不支持
-  InnoDB支持外键，而MyISAM不支持
-  InnoDB是聚集索引， MyISAM是非聚集索引
-  Innodb不支持全文索引，而MyISAM支持全文索引
-  InnoDB支持表、行(默认)级锁，而MyISAM支持表级锁
-  myisam 对count(*) 有优化
-  从数据库使用myisam可优化查询效率

# 2. javase

## 2.1 集合
关于集合的类图和继承关系这里不再赘述，不懂的请自行百度。这里主要分析的集合类有`Vector`、`TreeSet`、`HashMap`、`HashTable`、`TreeMap`，和`concurrent`包下的并发集合类。

先对集合框架做一个整体上的介绍：

- 顶层接口为`Collection` 和 `Map`,`Collection`的子接口有 `Set`、`list`、`Queue`
- `Collection`的子接口有 `Set`和`list`,Map的实现有 `HashMap TreeMap LinkedHashMap` 等

关于集合我会依次介绍以下几个知识点：泛型、迭代模式、扩容机制、各个集合特点介绍

### 2.1.1泛型
泛型的观念是类型参数化，直白的说就是将类型作为参数来使用，但实际上Java泛型是一种“伪泛型”——它的泛型概念只存在于编译期，在jvm中不存在泛型这种概念，被编译转成机器码后泛型被擦除成原始类型。我们以 `ArrayList` 为例：
```java
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable
{
public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
}
```
现在我 new 一个list `List<String> test=new ArrayList<>()` ，`ArrayList` 类编译过后你可以理解成它变成了这样：
```java
public class ArrayList extends AbstractList
        implements List, RandomAccess, Cloneable, java.io.Serializable
{
public boolean add(Object e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
}
```
相当于泛型只起到占位符的作用和给编译器检查类型的作用，当然这里的实现涉及到了 `泛型擦除` `泛型上下限` `桥接方法` 等的概念。这里控制文章的篇幅就不做介绍，感兴趣的自行百度。这就是伪泛型，在jvm里面它根本不知道有T这个东西。它和真正的泛型比起来是有差异的，在反射机制中提现的尤为明显，这个差异性留给小伙伴们去深入思考。

### 2.1.2 集合迭代器
我们知道遍历集合可以使用它的迭代器，这里的设计模式就是迭代器模式，我们对它的迭代器模式进行简单的分析。以Arraylist为例，在AarrayList里面有个内部类，它长这样：
```java
 private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        Itr() {}
        ......
}
```
这个就是它的迭代器， Itr 内部定义了三个 int 型的变量：cursor、lastRet、expectedModCount；

- cursor 表示下一个元素的索引位置，lastRet 表示上一个元素的索引位置；

- modCount 用于记录 ArrayList 集合的修改次数；

- expectedModCount预期被修改的次数；

### 2.1.3 面试问题

以hashmap为例，hashmap是最常被问到面试问题，hashmap 在Java8以前是采用数组加链表的底层数据结构，在java8以后进行了优化。

- 为什么扩容是2的n次幂

为了复制的时候减少hash冲突。HashMap为了存取高效，要尽量较少碰撞，就是要尽量把数据分配均匀，每个链表长度大致相同，这个实现就在把数据存到哪个链表中的算法
这个算法实际就是取模，hash%length。 但是，大家都知道这种运算不如位移运算快。
因此，源码中做了优化hash&(length-1)。 也就是说hash%length==hash&(length-1)。
而 2的n次幂刚好是 111111111....，不会发生有的hash值取不到的情况

- 为什么HashMap的在链表元素数量超过8时改为红黑树

当元素大于8个的时候，此时需要红黑树来加快查询速度，但是新增节点的效率变慢了。而使用二叉查找树在特殊情况下会退化成链表。

- hash冲突你还知道哪些解决办法？

(1)开放定址法 (2)链地址法 (3)再哈希法 (4)公共溢出区域法 HashMap中使用的是链地址法。

- 为什么load factor为0.75

这是一个经验值，没必要去纠结。

- hashmap和hashtable区别
Hashtable 线程安全 key value 都不能是null hashmap对 key 是null的值会放在数组的0位

- hashmap 的put方法

对key的hashCode()做hash运算，计算index; 如果没碰撞直接放到bucket里； 如果碰撞了，以链表的形式存在buckets后； 如果碰撞导致链表过长(大于等于TREEIFY_THRESHOLD)，就把链表转换成红黑树(JDK1.8中的改动)； 如果节点已经存在就替换old value(保证key的唯一性) 如果bucket满了(超过load factor*current capacity)，就要resize。

**延时队列 DelayedQuene**
将消息体放入延迟队列中，在启动消费者线程去消费延迟队列中的消息，如果延迟队列中的消息到了延迟时间则可以从中取出消息否则无法取出消息也就无法消费。



## 2.2 jdk 指令工具集

### 2.2.1 jar
jar 是jdk 提供的打jar包,熟悉该指令极其jar机制能让我们更好的理解 maven  ant等工具原理

**命令行打jar包**

首先我们建立一个普通的java项目，新建几个class类，然后在根目录下新建META-INF/MAINFEST.MF 这个文件包含了jar的元信息，当我们执行java -jar的时候首先会读取该文件的信息做相关的处理。

我们来看看这个文件中可以配置哪些信息 ：

- Manifest-Version：用来定义manifest文件的版本，例如：Manifest-Version: 1.0
- Main-Class：定义jar文件的入口类，该类必须是一个可执行的类，一旦定义了该属性即可通过 java -jar x.jar来运行该jar文件。
- Class-Path：指定该jar包所依赖的外部jar包，以当前jar包所在的位置为相对路径，无法指定jar包内部的jar包
- 签名相关属性，包括Name，Digest-Algorithms，SHA-Digest等

我们可以随便拆一个可执行jar查看相关信息，这里不赘述

命令行执行指令：

```
/* 1. 默认打包 */
// 生成的test.jar中就含test目录和jar自动生成的META-INF目录（内含MAINFEST.MF清单文件）
jar -cvf test.jar test

/* 2. 查看包内容 */
jar -tvf test.jar

/* 3. 解压jar包 */
jar -xvf test.jar

/* 4. 提取jar包部分内容 */
jar -xvf test.jar test\test.class

/* 5. 追加内容到jar包 */
//追加MAINFEST.MF清单文件以外的文件，会追加整个目录结构
jar -uvf test.jar other\ss.class

//追加清单文件，会追加整个目录结构(test.jar会包含META-INF目录)
jar -uMvf test.jar META-INF\MAINFEST.MF

/* 6. 创建自定义MAINFEST.MF的jar包 */
jar -cMvf test.jar test META-INF

// 通过-m选项配置自定义MAINFEST.MF文件时，自定义MAINFEST.MF文件必须在位于工作目录下才可以
jar -cmvf MAINFEST.MF test.jar test
```
**jar运行的过程**

jar运行过程和类加载机制有关，而类加载机制又和我们自定义的类加载器有关，现在我们先来了解一下双亲委派模式。

java中类加载器分为三个：

- BootstrapClassLoader负责加载${JAVA_HOME}/jre/lib部分jar包
- ExtClassLoader加载${JAVA_HOME}/jre/lib/ext下面的jar包
- AppClassLoader加载用户自定义-classpath或者Jar包的Class-Path定义的第三方包

类的生命周期为：加载（Loading）、验证（Verification）、准备(Preparation)、解析(Resolution)、初始化(Initialization)、使用(Using) 和 卸载(Unloading)七个阶段。

当我们执行 java -jar的时候 

jar文件以二进制流的形式被读取到内存，但不会加载到jvm中，类会在一个合适的时机加载到虚拟机中。类加载的时机：

- 遇到new、getstatic、putstatic或invokestatic这四条字节码指令时，如果类没有进行过初始化，则需要先对其进行初始化。成这四条指令的最常见的Java代码场景是使用new关键字实例化对象的时候，读取或设置一个类的静态字段调用一个类的静态方法的时候。
- 使用java.lang.reflect包的方法对类进行反射调用的时候，如果类没有进行过初始化，则需要先触发其初始化。
- 当初始化一个类的时候，如果发现其父类还没有进行过初始化，则需要先触发其父类的初始化。
- 当虚拟机启动时，用户需要指定一个要执行的主类（包含main()方法的那个类），虚拟机会先初始化这个主类。


当触发类加载的时候，类加载器也不是直接加载这个类。首先交给AppClassLoader，它会查看自己有没有加载过这个类，如果有直接拿出来，无须再次加载，如果没有就将加载任务传递给ExtClassLoader，而ExtClassLoader也会先检查自己有没有加载过，没有又会将任务传递给BootstrapClassLoader，最后BootstrapClassLoader会检查自己有没有加载过这个类，如果没有就会去自己要寻找的区域去寻找这个类，如果找不到又将任务传递给ExtClassLoader，以此类推最后才是AppClassLoader加载我们的类。这样做是确保类只会被加载一次。通常我们的类加载器只识别classpath（这里的classpath指项目根路径，也就是jar包内的位置）下.class文件。jar中其他的文件包括jar包被当做了资源文件，而不会去读取里面的.class 文件。但实际上我们可以通过自定义类加载器来实现一些特别的操作。

### 2.2.2 javap

javap 指令可以将我们的字节码文件转汇编和转操作码。实际上很多代码想真正的搞懂其底层原理都需要通过阅读操作码或者汇编才能明白，比如volitail,this等这些关键字底层原理。下面主要介绍Java操作码的内容

```
# 查看帮助手册
javap -help
# 具体参数意义自行查帮助手册
javap -v -l -p -s -sysinfo  -constants Test.class
```
输出的内容大概长这样：
```
Classfile ...
  
  ...
  
Constant pool:
  ...
  
{
    ...
}

```
`Classfile` 是一些类信息，`Constant pool` 是编译时常量池，{} 里面的内容是属性方法。现在我们开始对每一块的内容进行学习。

常量池的计数实际上是从0开始的，但是0位有特殊用途空出来的，所以在常量池中我们看不到0位。因为我的类中未定义静态常量，所以常量池中没有显示一些静态常量相关的信息。

`｛｝` 部分是方法部分，这里面的指令都能通过查操作码手册查到，这里只说几个常见代码代表的含义。

- ()V 表示无参的无返回值的方法描述符
- aload_0 从本地变量表中加载索引为0的变量的值，也即this的引用，压入栈
- invokestatic invokespecial invokeinterface 分别指调用静态方法 私有方法 接口方法。

这一块的东西本身不是很复杂，但是需要实操查表。

** 接下来我提一个问题，为什么动态代码块里面可以使用 this 关键字。不要太快给出答案，仔细思考一下把。（彩蛋哦）**

### 2.2.3 其他指令

虚拟机调优指令不在这里介绍了

- jdb Java debug 工具，通过各种参数对class设置断点调试
- jps Java进程查看工具
- javac java编译工具，想必大家都用过了


## 2.3 nio
（这一块 主要是面向面试，不适合没接触的）

nio有四个很重要的类：Selector，Channel，Buffer，Charset

Channel通过节点流的getChannel()方法来获得，成员map()用来将其部分或全部数据映射为Buffer，成员read()、write()方法来读写数据，而且只能通过Buffer作为缓冲来读写Channel关联的数据。

Channel接口下有用于文件IO的FileChannel，用于UDP通信的DatagramChannel，用于TCP通信的ocketChannel、ServerSocketChannel，用于线程间通信的Pipe.SinkChannel、Pipe.SourceChannel等实现类。Channel也不是通过构造器来创建对象，而是通过节点流的getChannel()方法来获得，如通过FileInputStream、FileOutputStream、andomAccessFile的getChannel()获得对应的FileChannel。

Channel中常用的方法有map()、read()、write()，map()用来将Channel对应的部分或全部数据映射成MappedByteBuffer（ByteBuffer的子类），read()/write()用于对Buffer读写数据。

Buffer是一个缓冲区，它是一个抽象类，常用的子类:ByteBuffer,MappedByteBuffer,CharBuffer,DoubleBuffer,FloatBuffer,IntBuffer,LongBuffer,ShortBuffer等，通过它可以用来装入数据和输出数据。Buffer没有构造器，使用类方法allocate()来创建对应的Buffer对象，当向Buffer写入数据后，在读取Buffer中数据之前应该调用flip()方法来设置Buffer中的数据位置信息，读取Buffer中数据之后应该调用clear()方法来清空原来的数据位置信息。compact()方法只会清除已经读过的数据。任何未读的数据都被移到缓冲区的起始处，新写入的数据将放到缓冲区未读数据的后面。

Charset可以将Unicode字符串（CharBuffer）和字节序列（ByteBuffer）相互转化。

Java中默认使用Unicode字符集，可以通过Charset来处理字节序列和字符序列（字符串）之间的转换，其availableCharsets()静态方法可以获得当前JDK支持的所有字符集。调用Charset的静态方法forName()可以获得指定字符集对应的Charset对象，调用该对象的newEncoder()、newDecoder()可以获得对应的编码器、解码器，调用编码器的encode()可以将CharBuffer或String转换为ByteBuffer，调用解码器的decode()可以将ByteBuffer转换为CharBuffer。

Selector允许单线程处理多个 Channel。如果你的应用打开了多个连接（通道），但每个连接的流量都很低，使用Selector就会很方便。Selector（选择器）是Java NIO中能够检测一到多个NIO通道，并能够知晓通道是否为诸如读写事件做好准备的组件。这样，一个单独的线程可以管理多个channel。通过调用Selector.open()方法创建一个Selector，将Channel注册到Selector上。通过SelectableChannel.register()方法来实现。与Selector一起使用时，Channel必须处于非阻塞模式下。这意味着不能将FileChannel与Selector一起使用，因为FileChannel不能切换到非阻塞模式。而套接字通道都可以。

缓冲区本质上是一块可以写入数据，然后可以从中读取数据的内存。这块内存被包装成NIO Buffer对象，并提供了一组方法，用来方便的访问该块内存。它的三个属性capacity,position和limit就是描述这块内存的了。capacity可以简单理解为这块内存的大小；写数据到Buffer中时，position表示当前的位置。初始的position值为0。当一个byte、long等数据写到Buffer后， position会向前移动到下一个可插入数据的Buffer单元。position最大可为capacity – 1.
当读取数据时，也是从某个特定位置读。当将Buffer从写模式切换到读模式，position会被重置为0. 当从Buffer的position处读取数据时，position向前移动到下一个可读的位置。limit表示你最多能读（写）多少数据。

buffer的方法：

- flip()：将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值。
- get()：从Buffer中读取数据
- rewind()：将position设回0，所以你可以重读Buffer中的所有数据。limit保持不变，仍然表示能从Buffer中读取多少个元素（byte、char等）。
- clear()：position将被设回0，limit被设置成 capacity的值。换句话说，Buffer 被清空了。Buffer中的数据并未清除，只是这些标记告诉我们可以从哪里开始往Buffer里写数据。
- compact()：将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。limit属性依然像clear()方法一样，设置成capacity。
- put()：向Buffer存入数据，带索引参数的版本不会移动位置position。
- capacity()：获得Buffer的大小capacity。
- hasRemaining()：判断当前位置position和界限limit之间是否还有元素可供处理。
- remaining()：获得当前位置position和界限limit之间元素的个数。
- limit()：获得或者设置Buffer的界限limit的位置。
- position()：获得或者设置Buffer的位置position。
- mark()：设置Buffer的mark位置。
- reset()：将位置positon转到mark所在的位置

** nio 零拷贝 **

nio 的零拷贝基本上是面试必问，如果往深了问甚至会问到 操作系统io复用的问题
这里简单都说说吧
零拷贝其实就是通过操作堆外内存来实现的，正常io会将操作的io数据复制到jvm虚拟机中然后操作。而零拷贝则是直接操作堆外内存。从操作系统层面的体现就是一个数据如果不采用零拷贝则数据需要从磁盘拷贝到内核空间，再从内核空间拷到用户空间，然后对数据处理，再将数据拷贝到内核空间，内核空间再拷贝到磁盘或者网卡内存，通过网络发送出去。

io多路复用的技术 select poll epoll(为Linux所特有) select 是轮训机制 epoll是事件触发机制。

select, poll是为了解決同时大量IO的情況（尤其网络服务器），但是随着连接数越多，性能越差

epoll是select和poll的改进方案，在 linux 上可以取代 select 和 poll，可以处理大量连接的性能问题


## 2.4 java8 新特性

主要掌握 stream和四类函数式接口：Supplier Consumer Function Predicate

## 2.5 spi
是Java提供的一套用来被第三方实现或者扩展的API，它可以用来启用框架扩展和替换组件。spi机制是这样的：读取META-INF/services/目录下的元信息，然后ServiceLoader根据信息加载对应的类，你可以在自己的代码中使用这个被加载的类。要使用Java SPI，需要遵循如下约定：

- 当服务提供者提供了接口的一种具体实现后，在jar包的META-INF/services目录下创建一个以“接口全限定名”为命名的文件，内容为实现类的全限定名；
- 接口实现类所在的jar包放在主程序的classpath中；
-主程序通过java.util.ServiceLoder动态装载实现模块，它通过扫描META-INF/services目录下的配置文件找到实现类的全限定名，把类加载到JVM；
- SPI的实现类必须携带一个不带参数的构造方法；

spi使用示例:
建一个maven项目，定义一个接口 (com.test.SpiTest)，并实现该接口（com.test.SpiTestImpl）；然后在 src/main/resources/ 下建立 /META-INF/services 目录， 新增一个以接口命名的文件 (com.test.SpiTest)，内容是要应用的实现类（com.test.SpiTestImpl）。

```java
public interface SpiTest {
    void test();
}


public class SpiTestImpl implements SpiTest {
    @Override
    public void test() {
        System.out.println("test");
    }
}
```

然后在我们的应用程序中使用 ServiceLoader来加载配置文件中指定的实现。

```java
public static void main(String[] args) {
        ServiceLoader<SpiTest> load = ServiceLoader.load(SpiTest.class);
        SpiTest next = load.iterator().next();
        next.test();
      
    }
```

spi技术的应用

那这一项技术有哪些方面的应用呢？最直接的jdbc中我们需要指定数据库驱动的全限定名，这便是spi技术。还有不少框架比如dubbo，都会预留spi扩展点比如：dubbo spi

为什么要这么做呢？在spring框架中我们注入一个bean 很容易，通过注解或者xml配置即可，然后在其他的地方就能使用这个bean。在非spring框架下，我们想要有同样的效果就可以考虑spi技术了。

写过springboot 的starter的都知道，需要在 src/main/resources/ 下建立 /META-INF/spring.factories 文件。这其实也是一种spi技术的变形。

## tomcat 和springboots类加载机制

Tomcat的类加载机制是违反了双亲委托原则的，对于一些未加载的非基础类(Object,String等)，各个web应用自己的类加载器(WebAppClassLoader)会优先加载，加载不到时再交给commonClassLoader走双亲委托。

tomcat的类加载器：

- Common类加载器：负责加载/common目录的类库，这儿存放的类库可被tomcat以及所有的应用使用。
- Catalina类加载器：负责加载/server目录的类库，只能被tomcat使用。
- Shared类加载器：负载加载/shared目录的类库，可被所有的web应用使用，但tomcat不可使用。
- WebApp类加载器：负载加载单个Web应用下classes目录以及lib目录的类库，只能当前应用使用。
- Jsp类加载器：负责加载Jsp，每一个Jsp文件都对应一个Jsp加载器。

我们将一堆jar包放到tomcat的项目文件夹下，tomcat 运行的时候能加载到这些jar包的class就是因为这些类加载器对读取到的二进制数据进行处理解析从中拿到了需要的类

当我们将一个springboot项目打好包之后，不妨解压看看里面的结构是什么样子的的：

```
run.jar
|——org
|  |——springframework
|     |——boot
|        |——loader
|           |——JarLauncher.class
|           |——Launcher.class
|——META-INF
|  |——MANIFEST.MF  
|——BOOT-INF
|  |——class
|     |——Main.class
|     |——Begin.class
|  |——lib
|     |——commons.jar
|     |——plugin.jar
|  |——resource
|     |——a.jpg

|     |——b.jpg
```

classpath可加载的类只有JarLauncher.class，Launcher.class，Main.class，Begin.class。在BOOT-INF/lib和BOOT-INF/class里面的文件不属于classloader搜素对象直接访问的话会报NoClassDefDoundErr异常。Jar包里面的资源以 Stream 的形式存在（他们本就处于Jar包之中），java程序时可以访问到的。当springboot运行main方法时在main中会运行org.springframework.boot.loader.JarLauncher和Launcher.class这两个个加载器（你是否还及得前文提到过得spi技术），这个加载器去加载受stream中的jar包中的class。这样就实现了加载jar包中的jar这个功能否则正常的类加载器是无法加载jar包中的jar的class的，只会根据MAINFEST.MF来加载jar外部的jar来读取里面的class。

 tomcat 类加载器面试问题

 - tomcat 类加载的流程
 - tomcat 是如何做到应用之间类隔离的。 

# 3 jvm 

## 3.1 运行时数据区域

 &emsp; &emsp;想要了解jvm，那对其内存分配管理的学习是必不可少的；java虚拟机在执行java程序的时候会把它所管理的内存划分成若干数据区域。这些区域有着不同的功能、用途、创建/销毁时间。java虚拟机所分配管理的内存区域如图1所示
 ![]()

### 3.1.1 程序计数器

&emsp; &emsp;程序计数器是一块比较小的内存空间，它可以看做是当前线程所执行的字节码的执行位置的指针。在虚拟机中字节码，解释器工作时就是通过改变这个计数器的值来选取下一条需要执行的指令；虚拟机完成分支、循环、跳转、异常处理、线程恢复等功能都需要依靠它。
&emsp; &emsp;我们知道jvm多线程是通过线程的轮流切换并分配处理器执行时间的的方式来实现的，在任何时刻，一个处理器都只会执行一条线程中的指令。为了使线程被切换后能恢复到正确的执行位置，每条线程的程序计数器都应该是独立的，各条线程之间的计数器互不干涉，独立存储————程序计数器的内存区域为线程私有的内存。<br/>
&emsp; &emsp;如果线程正在执行的是java方法，这个计数器记录的是正在执行的虚拟机字节码指令的地址；如果执行的是Native方法，这个计数器的值则为空。此内存区域是唯一一个在jvm规范中没有规定任何OutOfMemoryerror情况的区域

### 3.1.2 java虚拟机栈

&emsp; &emsp;java虚拟机栈为线程私有的内存，其生命周期与线程相同。每个方法在执行的时候会创建一个栈帧用于存储局部变量表、操作数栈、方法出口等信息。每一个方法从调用到执行完成，就对应着一个栈帧在虚拟机中从入栈到出栈的过程。其局部变量表存放了方法编译期可知的各种基本数据类型、对象引用、returnAddress类型（指向一条字节码指令的地址）jvm规范中，这个区域规定了两种异常状况：StackOverflowError和OutOfMemoryError。

### 3.1.3 本地方法栈

&emsp; &emsp;本地方法栈的作用和虚拟机栈的作用很相似，它们的区别在于虚拟机栈为虚拟机执行java方法服务，而本地方法栈则为执行本地方法服务。有的虚拟机直接把本地方法栈和虚拟机栈二合一。与虚拟机栈一样，本地方法栈的异常也有两个：StackOverflowError和OutOfMemoryError。

### 3.1.4 java堆区

&emsp; &emsp;java堆是虚拟机所管理的内存中最大的一块，它是被所有线程共享的一块内存区域，该区域在虚拟机启动的时候创建。这个区域的唯一目的就是存放对象实例。java堆是垃圾收集器工作的主要区域，由于垃圾收集器基本都采用分代收集的算法，所以java堆从垃圾收集器的角度来划分可以细分为新生代和老年代；从内存分配的角度来看，线程共享的java堆可能划分出多个线程私有的分配缓冲区。<br/>
&emsp; &emsp;java堆区可以是物理上不连续的内存空间，只要逻辑上是连续的即可；一般而言我们的虚拟机java堆内存不是固定大小的，是可以扩展的。如果在堆中没有足够内存分配给对象实例，并且堆内存无法再扩展时，虚拟机将会抛出OutOfMemoryError异常。

### 3.1.5 方法区

&emsp; &emsp;方法区与java堆区一样是各个线程共享的内存区域，这个区域存储了类信息、常量、静态变量等数据。java虚拟机规范中把方法区描述为堆得一部分逻辑，它又有一个名字——非堆，目的是与普通java堆进行区分。相对而言垃圾收集器在这个区域很少活动，因此一部分人把这个区域叫做“永久代”。这个区域的内存回收目标主要是针对常量池的回收和类型的卸载，然而类型卸载的条件是很苛刻的。该区域和和java堆区一样，当内存不够分配时会抛出OutOfMemoryError.

### 3.1.6 运行时常量池

&emsp; &emsp;运行时常量池是方法区的一部分；一个Class文件中除了有类的版本、字段、方法、接口等描述信息外，还有一项信息是编译时常量池，用于存放编译期生成的常量。编译时常量池在类被加载后会放入方法区的运行时常量池中。与编译期常量池不同的是，运运行时常量池是动态的，运行期间产生的新的常量也会被放入这个区域，如：String类的intern()方法。

## 3.2 对象

### 3.2.1 对象的创建

&emsp; &emsp;在语言层面上，创建一个对象通常是通过new关键字来创建，在虚拟机中遇到一条new指令时，首先将去检查这个指令的参数是否能在常量池中定位到一个类的符号引用，并且检查这个符号引用代表的类是否已被加载、解析和初始化过；如果没有的话就会先加载这个类；类加载检查完后，虚拟机将会为新生对象分配内存。对象所需内存的大小在类加载完成后便可完全确定，在堆中为对象划分一块内存出来。

&emsp; &emsp;虚拟机给对象分配内存的方式有两种——“指针碰撞”的方式和“空闲列表”的方式。如果java堆内存是绝对规整的，所有用过的内存放在一边，未使用的内存放在另一边，中间放一个指针作为指示器，那分配内存就只是把指针向未使用区域挪一段与对象大小相等的距离；这种分配方式叫指针碰撞式，如图1所示。

**图中水印是我以前公众号的名字，并非盗图（现改名为六个核弹）**

![图1：指针碰撞式内存分配方式](http://upload-images.jianshu.io/upload_images/13612520-35177fc9f287a7f3?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

我们知道，堆内存随时都可能被垃圾收集器回收的，当内存被回收后堆内存就可能不是连续的了，所以当采用指针碰撞的方式时，垃圾收集器必须有内存整理的功能，能对垃圾回收后的零散内存进行整理。而空闲列表的方式则不需要垃圾收集有这个功能，采用这种方式时虚拟机会维护一张表，用于记录那些内存是可用的，当需要分配内存时就从表中找出一块足够的内存进行分配，并记录在表上。

&emsp; &emsp;内存分配完成后，虚拟机需要将分配到的内存空间都初始化；接下来虚拟机会对对象进行必要的设置，例如这个对象是哪个类的实例，如何才能找到类的元数据信息、对象的哈希值、对象的GC的分代年龄等信息。这些信息存在对象的对象头之中。完成这些工作后，从虚拟机的角度来看一个新的对象就产生了，但从程序的角度来看对象创建才刚刚开始，对象尚未执行初始化方法，各个字段都还未赋值，接下来会执行初始化方法，只有在执行初始化方法后，一个真正可用的对象才算是被创建。

### 3.2.2 对象的内存

在HotSpot虚拟机中，对象在内存中分为三块区域：对象头、实例数据、和对齐填充。对象头包括两部分信息，第一部分用于存储对象自身运行的运行时数据，如哈希码、GC分代年龄、锁状态标志线程持有的锁等。对象头的另外一部分是类型指针，即对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例
。接下来的实例数据部分是对象真正存储的有效信息，也是在代码中所定义的各个字段的内容。这些字段无论是在父类那继承过来的还是子类里定义都要记录下来。第三部分对齐填充不是必然存在的，它仅仅起占位符的作用，用以填充内存。

### 3.2.3 对象的访问定位

  &emsp; &emsp;建立对象是为了使用对象，我们的java程序需要通过栈上的reference来操作堆上的对象。通过reference来访问对象的方法有两种——使用句柄和直接指针。在虚拟机执行一个方法时，虚拟机栈 中会为方法分配一个 局部变量表，一个操作数栈；局部变量表是用于保存函数的参数以及局部变量的，其保存的类型有boolean、byte、char、short、int、float、reference和returnAddress八种；方法在执行的过程中，会有各种各样的字节码指令往操作数栈中执行入栈和出栈操作，完成数据的运算。基本数据类型直接存储到变量表中。那reference是如何找到引用的对象的呢？

  &emsp; &emsp;如果使用句柄的话，那么会在java堆中划分一块内存来作为句柄池，reference中存储的是句柄的地址，而句柄中包含了对象的具体地址信息，如图2所示
![图:2：通过句柄访问对象](http://upload-images.jianshu.io/upload_images/13612520-444340fc1999cdc6?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

  &emsp; &emsp;如果使用直接指针访问，那么java堆对象的布局则如图3所示；
![图3：通过直接指针访问对象](http://upload-images.jianshu.io/upload_images/13612520-cfd7ee35d69ee849?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## 3.3 垃圾收集器

&emsp; &emsp;java内存在运行时被分为多个区域，其中程序计数器、虚拟机栈、本地方法栈三个区域随线程生成和销毁；每一个栈帧中分配多少内存基本上是在类结构确定下来时就已知的，在这几个区域内就不需要过多考虑回收问题，因为方法结束或者线程结束时，内存自然就跟着回收了。而堆区就不一样了，我们只有在程序运行的时候才能知道哪些对象会被创建，这部分内存是动态分配的，垃圾收集器主要关注的也就是这部分内存。

### 3.3.1 垃圾收集器算法

&emsp; &emsp;jdk11刚发布不久，这个版本发布了一款新的垃圾收集器——G1垃圾收集器,这款垃圾收集器有很多优异的特性，我会在后文做介绍，这里先从简单的慢慢说起。

&emsp; &emsp;引用计数算法是最初垃圾收集器采用的算法，也是相对简单的一种算法，其原理是：给对象中添加一个引用计数器，每当有一个地方引用它的时候这个计数器就加一；当引用失效，计数器就减一；任何时刻计数器为0则该对象就会被垃圾收集器回收。这种算法的缺点是当对象之间相互循环引用的时候，对象将永远不会被回收。举个例子——有类TestOne,类TestTwo;它们互相是对方的成员，如下：

```java
 public static void main(String[] args) {
    TestOne testOne=new TestOne();
    TestTwo testTwo=new TestTwo();
    testOne.obj=testTwo;
    testTwo.obj=testOne;
    testOne=null;
    testTwo=null;
}

```

理论上当代码执行到testTwo=null的时候 new TestOne() new TestTwo() 两块内存应该要被回收的，但是因为它们相互引用对方导致引用计数器不为0，所以这两块内存没有引用指向它们却无法被回收——这便是这种算法所存在的问题。

&emsp; &emsp;可达性分析算法是使用比较广泛的算法。这个算法的基本思路是通过一系列的称为“GC Roots”的对象作为起点，从这些节点向下搜索，搜索所走过的路径称作引用链；当一个对象和GC  oots之间不存在引用链的时候，这个对象将被回收；也就是说一个存活的对象向上追溯它的引用链，其头部必然是GC Roots,如果不是将被回收。在虚拟机中可以作为GC Roots的可以是：虚拟机栈中引用的对象、方法区中类静态属性引用的对象、方法区中常量引用的对象，本地方法栈中Native方法引用的对象；在堆区一个存活的对象被这些对象所直接引用或间接引用(引用又分为强引用、软引用、弱引用、、虚引用，引用强调依次降低，感兴趣的可以详细了解一下)。
&emsp; &emsp;当一个对象的引用链中没有GC Roots的时候并不会被马上回收，第一次他会被标记并筛选，当对象没有覆盖finalize()方法或该方法已经被虚拟机调用过，那么它会被放入一个叫做F-Queue的队列中等待被虚拟机自动回收；否则虚拟机会执行finalize()方法——当我们没有重写finalize()方法时，对象内存自然被回收掉，如果重写了这个方法，那么结果就会变得很有趣，下面做一个示例：

```java
public class Main {
    public static  Main test=null;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("执行了一次 finalize()");
        Main.test=this;
    }

    public static void main(String[] args) {
        test=new Main();
        // 让test失去 GC RootS
        test=null;
        // 调用 finalize()方法
        System.gc();
        // sleep一会确保finalize()方法执行
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 因为在finalize()方法中重新将this(也就是 new Main())赋值给了test 所以没被回收
        if(test!=null){
            System.out.println("对象存活了下来");
        }else{
            System.out.println("对象死了");
        }
        // 再来一次
        test=null;
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 这一次却死了，因为finalize()方法已经被执行过，虚拟机直接将对象扔到 F-Queue里面等待回收
        if(test!=null){
            System.out.println("对象存活了下来");
        }else{
            System.out.println("对象死了");
        }
    }

}
```

运行结果：

> 执行了一次 finalize()<br/>
> 对象存活了下来<br/>
> 对象死了

### 3.3.2 回收方法区

&emsp; &emsp;因为方法区的内存回收条件很苛刻，因此方法区被人称作永久代，在这个区域回收的内存主要为废弃的常量和无用的类；那么如何判定一个常量是否废弃呢？比如当一个字符串进入了常量池，但没有任何地方引用它，如果此时发生了内存回收，那么这个常量就会被清除出常量池——发生场景：一个类有一个成员 pubulic static String test="aaa";当这个类被加载的时候"aaa"进入常量池，当其他地方没有字符串等于"aaa"的时候并且此时这个类由于某种原因被卸载掉，此时这个"aaa"将会被回收。如何判定一个类是无用的类呢？需要满足三个条件：

> 该类所有的实例都被回收<br/>
> 加载该类的ClassLoader已经被回收
> <br/>该类的Class对象没在任何地方被引用，无法通过反射访问该类

### 3.3.3 分代收集


## 3.4 垃圾收集算法

### 3.4.1 标记-清除算法

标记-清除算法是最基础的算法，算法分为标记和清除两个阶段，首先标记出要清除的对象，在标记完后统一回收所有被标记的对象，标记方式为j《jvm系列之垃圾收集器》里面所提到的。这种算法标记和清除两个过程效率都不高；并且在标记清除后，内存空间变得很零散，产生大量内存碎片。当需要分配一个比较大的对象时有可能会导致找不到足够大的内存。<!--more-->

标记清除算法图解（图片来源于百度图片）：![timg.jpg](https://upload-images.jianshu.io/upload_images/13612520-e59da44ca1b963c6.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240) 

### 3.4.2 清除-复制算法

&emsp;为了解决标记清除效率低的问题，出现了复制算法；这种算法将内存划分为大小相等的两块内存，只使用其中一块。当这一块内存使用完了就将存活的对象复制到另一块上面去，然后把已使用的内存空间一次性清理掉，这种方法不必考虑内存碎片的情况，运行高效，实现简单。缺点是浪费了一半的内存。复制算法图解（图片来源百度图片）：![timg (1).jpg](https://upload-images.jianshu.io/upload_images/13612520-2f12466c88adfd82.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 3.4.3 标记-整理算法

&emsp; &emsp;复制收集算法在对象存活率较高的时候就要进行较多的复制操作，导致效率变低。而且老年代很少会有内存回收，对老年代而言，复制算法做了大量的无用功。针对复制算法存在的的问题，有人提出了标记-整理算法。标记过程和标记-清除算法过程一样，但后续不是直接对可回收对象进行清理，而是让所有存活对象都向一方移动，整理内存，然后再进行清理。标记-整理算法图解（图片来源百度图片）：![timg (2).jpg](https://upload-images.jianshu.io/upload_images/13612520-4fd6dd6461485a3c.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 3.4.4 分代收集算法

&emsp; &emsp;分代收集算法思路是根据对象存活周期不同将内存划分为几块。一般是分为新生代和老年代，这样就可以根据各个年代的特点采用最适当的收集算法。在新生代中每次收集时都会回收很多内存，选用高效率的复制算法，并且只需要预留少量的复制空间，用于复制存活对象。老年代中因为对象存活率高，采用标记-整理或标记清理算法节省内存空间提高清理效率。

### 3.4.5 各版本jdk垃圾收集器一览

| 收集器名称        | 区 &emsp; 域  | 说明  &emsp; &emsp;&emsp; &emsp;&emsp; &emsp;&emsp; &emsp;&emsp; &emsp;&emsp; &emsp;&emsp; &emsp;&emsp; &emsp;&emsp; &emsp;&emsp; &emsp; |
| ----------------- | :-----------: | -----------------------------------------------------------: |
| Serial            |    新生代     | 单线程，GC时必须停止其它线程直到收集结束；JVM运行在client模式下新生代的默认收集器，简单有效；采用复制算法 |
| ParNew            |    新生代     | Serial收集的多线程版，保留Serial的参数控制，算法等，暂停所有用户线程，采用复制算法；JVM运行在server的首先的新生代收集器；只有它能和CMS配合工作 |
| Parallel Scavenge |    新生代     | 采用复制算法，并行的多线程收集器，与ParNew不同的是，关注点不是停顿时间，而是可控制的吞吐量，即运行用户代码的时间/（运行用户代码的时间+垃圾收集的时间）。可设置最大GC时间和吞吐量大小等参数，也可以让JVM自适应调整策略 |
| CMS               |    新生代     | concurrent Mark Sweep，已获取最短回收停顿为目标，大部分的互联网站及服务端采用的方式，标记-清除算法 |
| G1                | 新生代/老年代 |                   收集器最前沿版本，JDK 1.7，代替CMS的新产品 |
| Serial Old（MSC） |    老年代     | Serial的老年版，单线程收集器，采用标记-整理算法，主要是client模式的JVM使用 |
| Parallel Old      |    老年代     |              Parallel Scavenge的老年版，多线程，标记整理算法 |

### 3.4.6 jdk11 垃圾收集器——ZGC

&emsp; &emsp;（网上搜的）ZGC是一个处于实验阶段的，可扩展的低延迟垃圾回收器，旨在实现以下几个目标：

- 停顿时间不超过10ms
- 停顿时间不随heap大小或存活对象大小增大而增大
- 可以处理从几百兆到几T的内存大小

限制：

- 当前版本不支持类卸载
- 当前版本不支持JVMCI

ZGC包含10个阶段，但是主要是两个阶段标记和relocating。GC循环从标记阶段开始，递归标记所有可达对象，标记阶段结束时，ZGC可以知道哪些对象仍然存在哪些是垃圾。ZGC将结果存储在每一页的位图（称为live map）中。在标记阶段，应用线程中的load barrier将未标记的引用压入线程本地的标记缓冲区。一旦缓冲区满，GC线程会拿到缓冲区的所有权，并且递归遍历此缓冲区所有可达对象。注意：应用线程负责压入缓冲区，GC线程负责递归遍历。

&emsp; &emsp;标记阶段后，ZGC需要迁移relocate集中的所有对象。relocate集是一组页面集合，包含了根据某些标准（例如那些包含最多垃圾对象的页面）确定的需要迁移的页面。对象由GC线程或者应用线程迁移（通过load barrier）。ZGC为每个relocate集中的页面分配了转发表。转发表是一个哈希映射，它存储一个对象已被迁移到的地址（如果该对象已经被迁移）。GC线程遍历relocate集的活动对象，并迁移尚未迁移的所有对象。有时候会发生应用线程和GC线程同时试图迁移同一个对象，在这种情况下，ZGC使用CAS操作来确定胜利者。一旦GC线程完成了relocate集的处理，迁移阶段就完成了。虽然这时所有对象都已迁移，但是旧地引用址仍然有可能被使用，仍然需要通过转发表重新映射（remapping）。然后通过load barrier或者等到下一个标记循环修复这些引用。


## 3.5 类加载机制

### 3.5.1 类的生命周期

&emsp; &emsp;类从被加载到虚拟机内存中内存中开始，到卸载出内存为止，它的整个生命周期包括：加载（loading）、验证（verification）、准备（preparation）、解析（resolution）、初始化（initialization）、使用（using）卸载（unloading）七个阶段。其中验证、准备、解析三个阶段统称为连接（linking）。


### 3.5.2 加载

&emsp; &emsp;加载是类加载机制的第一个阶段，在这个阶段，虚拟机做了三件事情：

> - 通过类的全限定名来获取定义此类的二进制字节流；

- 将这个字节流所代表的静态存储结构转换为方法区的运行时数据结构
- 在内存中生成一个代表这个类的Class对象，作为方法区这个类的各种数据的访问入口

&emsp; &emsp;加载阶段完成后，虚拟机外部的二进制字节流就按照虚拟机所需的格式存储在方法区之中，方法区中的数据存储格式由虚拟机实现自行定义；然后在内存中实例化一个Class类的对象，加载阶段和连接阶段的部分内容是交叉进行的，加载阶段尚未完成，连接阶段可能已经开始了。

### 3.5.3 验证

&emsp; &emsp;验证是连接阶段的第一步，这一阶段目的是为了确保Class文件的字节流中包含的信息符合当前虚拟机的要求，并且不会危害虚拟机自身的安全。从整体上来看，验证阶段包括以下四个动作：文件格式验证、元数据验证、字节码验证、符号引用验证。

### 3.5.4 准备

&emsp; &emsp;准备阶段是正式为类变量分配内存并设置类变量初始值得阶段，这些变量所使用的内存都将在方法区中进行分配。这个阶段中有两个容易产生混淆的概念——1.这个时候进行内存分配的仅包括类变量（被static修饰的变量），而不包括实例变量，2.这里初始值是数据类型的零值，假设一个类变量定义为 ：

```java
public static int value=2;
```

那变量value在准备阶段的值为0而不是2.

### 3.5.5 解析

&emsp; &emsp;解析阶段是虚拟机将常量池内的符号引用替换为直接引用的过程。

### 3.5.6 初始化

&emsp; &emsp;类初始化阶段是类加载过程的最后一步，前面的类加载过程中，除了在加载阶段可以通过自定义类加载器参与之外，其余动作都是虚拟机控制的。到了初始化阶段，才真正的执行java代码。初始化阶段是执行类构造器<clinit>()方法的过程。

&emsp; &emsp;想要使用一个类，必须对其进行初始化，但初始化过程不是必然执行的；jvm规范中规定有且只有以下五种情况必须对类进行初始化：

> - 遇到new、getstatic、putstatic、invokestatic这四个字节码指令的时候，如果类没有进行初始化，则需要先触发其初始化。生成这四条指令最常见的java代码场景是：使用new创建对象、读取或者设置一个类的静态字段（不包括值已在常量池中的情况）、调用一个类的静态方法的时候；

- 使用java反射机制的时候，如果类没初始化需要先初始化；
- 当初始化一个类的时候，如果发现其父类还未初始化，则需要先初始化父类。
- 当虚拟机启动时，用户需要指定一个要执行的主类，虚拟机会先初始化那个类。

以上五种情况称为对一个类进行主动引用；其他引用类的方式都不会触发初始化，称为被动引用。下面举一个被动引用的例子：

```java
public class TestClassloading {
    static {
        System.out.println("父类被初始化");
    }
    public static int number=111;
}

public class SubClass extends TestClassloading {
    static {
        System.out.println("子类被初始化");
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println(SubClass.number);
    }
}
```

输出结果：

```java
父类被初始化
111
```

显然，子类没有被初始化，这里SubClass.number为被动引用，不会对子类初始化。

### 3.5.7 类加载器

&emsp; &emsp;通过一个类的全限定名来获取描述此类的二进制字节流这个动作被放到虚拟机外部区实现，以便让应用程序自己决定如何去获取所需的类，实现这个动作的代码模块称为类加载器。对于任意一个类，都需要由加载它的类加载器和这个类本身一同确立其在java虚拟机中的唯一性，每一个类加载器都拥有一个独立的类名称空间。也就是说比较两个类是否相等必须要类加载器和类都相等。
&emsp; &emsp;从java虚拟机的角度来讲，只存在两种不同的类加载器：一种是启动类加载器，这个类加载器是虚拟机的一部分；另一种就是java代码实现的独立于虚拟机外部的类加载器，这种类加载器继承类抽象类java.lang.TestClassloader。

&emsp; &emsp;类加载器还有一个很重要的概念就是双亲委派模型——在类加载器工作的时候是多个类加载器一起工作的它们包括：扩展类加载器，应用程序类加载器，启动类加载器，自定义类加载器。类加载器的层次图如图：

![](https://upload-images.jianshu.io/upload_images/13612520-dd9a6a8324f136f8?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

类加载器层次结构

&emsp; &emsp;双亲委派模型的工作流程是如果一个类加载器收到了类加载的请求，它首先不会自己去尝试加载这个类，而是把这个请求交给父类加载器去完成，每一个层次的类加载器都是如此，因此所有的加载请求最终都会被交给启动类加载器，当父类反馈无法加载这个类的时候，子类才会进行加载。

Java的类加载机制：
- BootstrapClassLoader负责加载${JAVA_HOME}/jre/lib部分jar包
- ExtClassLoader加载${JAVA_HOME}/jre/lib/ext下面的jar包
- AppClassLoader加载用户自定义-classpath或者Jar包的Class-Path定义的第三方包

`BootstrapClassLoader` 为C语言编写的加载器，它会负责加载包括 `ExtClassLoader` 和 `AppClassLoader` 等的 `java.*` 和 `sun.*` 包下的类。而 `ExtClassLoader` 和 `AppClassLoader` 在内的类加载器， 实质性的类加载也需要依托于 `JNI` 机制

## 3.6 引用类型

在JDK 1.2以前的版本中，若一个对象不被任何变量引用，那么程序就无法再使用这个对象。对象引用被划分成简单的两种状态：可用和不可用。从JDK 1.2版本以后，对象的引用被划分为`4`种级别，从而使程序能更加灵活地控制对象的生命周期，引用的强度由高到低为：强、软、弱、虚引用。

对象生命周期：在JVM运行空间中，对象的整个生命周期大致可以分为7个阶段：创建阶段（Creation）、应用阶段（Using）、不可视阶段（Invisible）、不可到达阶段（Unreachable）、可收集阶段（Collected）、终结阶段（Finalized）与释放阶段（Free）。上面的这7个阶段，构成了 JVM中对象的完整的生命周期。

### 3.6.1 强引用(StrongReference)

强引用是使用最普遍的引用。如果一个对象具有强引用，那垃圾回收器绝不会回收它，我们使用new关键字就是创建了一个强引用。被强引用引用的内存是无法被GC回收的，想要回收这一块的内存得等这个引用从栈内存中出来，对应的内存无引用了才能被回收。

### 3.6.2 软引用(SoftReference)

如果一个对象只具有软引用，则内存空间充足时，GC不会回收这块内存；单如果内存不足的时候它就会被回收，只要垃圾回收器没有回收它，该对象就可以被程序使用。软引用可用来实现内存敏感的高速缓存。

创建一个软引用的办法

```java
    String str = new String("xxx");
    SoftReference<String> softReference = new SoftReference<String>(str);
```

`softReference`就是一个软引用。

当内存不足时，`JVM`首先将软引用中的对象引用置为`null`，然后通知垃圾回收器进行回收。也就是说当软引用指向null的时候，对应的内存可能还是未被GC回收的。虚拟机会尽可能的优先回收长时间闲置不用的软引用对象。

### 3.6.3 弱引用(WeakReference)

弱引用比软引用有更短暂的生命周期。在GC扫描内存区域的时候，一旦发现弱引用就会马上回收它。

创建一个弱引用的方法：

```java
String str = new String("xxx");
WeakReference<String> weakReference = new WeakReference<>(str);
// 弱引用转强引用
String strongReference = weakReference.get();
```

如果一个对象是偶尔(很少)的使用，并且希望在使用时随时就能获取到，但又不想影响此对象的垃圾收集，那么你应该用Weak Reference来记住此对象。如上面代码所示，弱引用也也可以转换成强引用

### 3.6.4 虚引用(PhantomReference)

虚引用可以理解为形同虚设的引用，不管你这个引用指向的内存有没有在用，它都随时可能被回收掉。虚引用必须和引用队列(ReferenceQueue)联合使用。当垃圾回收器准备回收一个对象时，如果发现它还有虚引用，就会在回收对象的内存之前，把这个虚引用加入到与之关联的引用队列中。

创建虚引用的办法：

```java
 String str = new String("xxx");
ReferenceQueue queue = new ReferenceQueue();
// 创建虚引用，要求必须与一个引用队列关联
PhantomReference pr = new PhantomReference(str, queue);
```

虚引用基本上好像没啥卵用。

GC线程在虚拟机中的优先级别很低的，因此占用cpu资源的机会很少，所以当一个内存变成非强引用的时候，不一定马上会被回收，而是看这个时候GC线程有没有在执行。如果GC在执行，它会先检查这个内存有没有有引用指向它，如果没有就回收，如果有那么根据引用的级别来采用垃圾回收策略。

### 3.7 面试问题

- CMS和G1的区别

  CMS收集器是老年代的收集器，可以配合新生代的Serial和ParNew收集器一起使用 G1收集器收集范围是老年代和新生代。不需要结合其他收集器使用 CMS收集器是使用“标记-清除”算法进行的垃圾回收，容易产生内存碎片 G1收集器使用的是“标记-整理”算法，进行了空间整合，降低了内存空间碎片。

- jvm gc过程

jvm 堆内存划为 年轻代（Eden区+两个Survivor，两个s去是为了算法的交替使用） 老年代 和持久代，新创建的的对象活在Eden区，当Eden区满触发 Minor GC，将Eden区 和一个Survivor活下来的对象放到另外一个Survivor区，然后清空这两个区。当一个对象年龄大于15岁（经历了15次gc,默认值，可配置）移入老年代。Eden区大对象会直接移入老年代，空间分配担保机制也会将对象移入老年代（一下过来太多对象，Eden内存不够用，直接就将对象放入老年代腾出空间了），老年代满发生full GC .full gc之后如果内存不够，内存溢出。

内存溢出快照分析
```
## 生成内存快照
 -XX:+HeapDumpOnOutOfMemoryError 
```

# 4. 并发

## 4.1 基本概念

### 4.1.1同步和异步

同步和异步通常来形容一次方法的调用。同步方法一旦开始，调用者必须等到方法结束才能执行后续动作；异步方法则是在调用该方法后不必等到该方法执行完就能执行后面的代码，该方法会在另一个线程异步执行，异步方法总是伴随着回调，通过回调来获得异步方法的执行结果；

### 4.1.2 并发和并行

很多人都将并发与并行混淆在一起，它们虽然都可以表示两个或者多个任务一起执行，但执行过程上是有区别的。并发是多个任务交替执行，多任务之间还是串行的；而并行是多个任务同时执行，和并发有本质区别。
对计算机而言，如果系统内只有一个cpu，而使用多进程或者多线程执行任务，那么这种情况下多线程或者多进程就是并行执行，并行只可能出现在多核系统中。当然，对java程序而言，我们不必去关心程序是并行还是并发。

### 4.1.3 临界区

临界区表示的是多个线程共享但同时只能有一个线程使用它的资源。在并行程序中临界区资源是受保护的，必须确保同一时刻只有一个线程能使用它。

### 4.1.4 阻塞

如果一个线程占有了临界区的资源，其他需要使用这个临界区资源的线程必须在这个临界区进行等待——线程被挂起，这种情况就是发生了阻塞——线程停滞不前。

### 4.1.5 死锁\饥饿\活锁

死锁就是多个线程需要其他线程的资源才能释放它所拥有的资源，而其他线程释放这个线程需要的资源必须先获得这个线程所拥有的资源，这样造成了矛盾无法解开；如图1情形就是发生死锁现象：

![](http://a2.qpic.cn/psb?/V13ysUCU2bV4he/zBrKU1zKzRRphjYm8*58YnBjOH0x7EvRxnWkrr.0oeE!/b/dMEAAAAAAAAA&ek=1&kp=1&pt=0&bo=2QENAQAAAAARF*Q!&tl=3&vuin=1793769323&tm=1555678800&sce=60-2-2&rf=viewer_4)

<center>图1：生活中的死锁现象</center>

活锁就是两个线程互相谦让资源，结果就是谁也拿不到资源导致活锁；就好比过马路，行人给车让道，车又给行人让道，结果就是车和行人都停在那不走。

饥饿就是，某个线程优先级特别低老是拿不到资源，导致这个线程一直无法执行

### 4.1.6 并发级别

并发级别分为阻塞，无饥饿，无障碍，无锁，无等待几个级别；根据名字我们也能大概猜出这几个级别对应的什么情形；阻塞，无饥饿和无锁都好理解；我们说一下无障碍和无等待；

无障碍：无障碍级别默认各个线程不会发生冲突，不会互相抢占资源，一旦抢占资源就认为线程发生错误，进行回滚。

无等待：无等待是在无锁上的进一步优化，限制每个线程完成任务的步数；

### 4.1.7 并行的两个定理

加速比：加速比=优化前系统耗时/优化后系统耗时

Amdahl定理： 加速比=1/[F+(1-F)/n] 其中 n表示处理器个数 ，F是程序中只能串行执行的比例——串行率；由公式可知，想要以最小投入，得到最高加速比即 F+(1-F)/n取到最小值，F和n都对结果有很大影响，在深入研究就是数学问题了；

Gustafson定律： 加速比=n-F(n-1)，这两定律区别不大，都体现了单纯的减少串行率，或者单纯的加CPU都无法得到最优解。

## 4.2 Java中的并行基础

### 4.2.3 volatile关键字和程序的原子性，可见性，有序性

原子性指的是一个操作是不可中断的，要么成功要么失败，不会被其他线程所干扰；比如 int=1,这一操作在cpu中分为好几个指令，但对程序而言这几个指令是一体的，只有可能执行成功或者失败，不可能发生只执行了一半的操作；对不同CPU而言保证原子性的的实现方式各有不同，就英特尔CPU而言是使用一个lock指令来保证的。

可见性指某一线程改变某一共享变量，其他线程未必会马上知道。

有序性指对一个操作而言指令是按一定顺序执行的，但编译器为了提高程序执行的速度，会重排程序指令；cpu在执行指令的时候采用的是流水线的形式，上一个指令和下一个指令差一个工步。比如A指令分三个工步：1. 操作内存a，2.操作内存b，3.操作内存c；现假设有个指令B操作流程和A一样，那么先执行指令A在执行指令B时间全利用上了，中间没有停顿等待；但如果有三个这样的指令在流水线上执行：a>b>c，b>e>c，c>e>a；这样的指令顺序就会发生等待降低了CPU的效率，编译器为了避免这种事情发生，会适当优化指令的顺序进行重排。

volatile关键字在java中的作用是保证变量的可见性和防止指令重排。

### 4.2.4 线程的相关操作

*创建线程有三种方法*

- 继承Thread类创建线程
- 实现Runnable接口创建线程
- 使用Callable和Future创建线程

*终止线程的方法*

终止线程可调用stop()方法，但这个方法是被废弃不建议使用的，因为强制终止一个线程会引起数据的不一致问题。比如一个线程数据写到一半被终止了，释放了锁，其他线程拿到锁继续写数据，结果导致数据发生了错误。终止线程比较好的方法是“让程序自己终止”，比如定义一个标识符，当标识符为true的时候直让程序走到终点，这样就能达到“自己终止”的目的。

*线程的中断等待和通知*

interrupt()方法可以中断当前程序，object.wait() 方法让线程进入等待队列，object.notify()随机唤醒等待队列的一个线程， object.notifyAll()唤醒等待队列的所有线程。object.wait()必须在synchronzied语句中调用；执行wait，notify方法必须获得对象的监视器，执行结束后释放监视器供其他线程获取。

*join*

join()方法功能是等待其他线程“加入”，可以理解为将某个线程并为自己的子线程，等子线程走完或者等子线程走规定的时间，主线程才往下走；join的本质是调用调用线程对象的wait方法，当我们执行wait或者notify方法不应该获取线程对象的的监听器，因为可能会影响到其他线程的join。

*yield*

yield是线程的“谦让”机制，可以理解为当线程抢到cpu资源时，放弃这次资源重新抢占，yield()是Thread里的一个静态方法。

### 4.2.5 线程组

如果一个多线程系统线程数量众多而且分工明确，那么可以使用线程组来分类。

```java
	
    @Test
    public void contextLoads() {
        ThreadGroup testGroup=new ThreadGroup("testGroup");
        Thread a = new Thread(testGroup, new MyRunnable(), "a");
        Thread b = new Thread(testGroup, new MyRunnable(), "b");
        a.start();
        b.start();
        int i = testGroup.activeCount();
    }

    public static class MyRunnable implements Runnable{
        @Override
        public void run() {
            System.out.println("test");
        }
    }
```

图示代码创建了一个"testGroup"线程组。

### 4.2.6 守护线程

守护线程是一种特殊线程，它类似java中的异常系统，主要是概念上的分类，与之对应的是用户线程。它功能应该是在后台完成一些系统性的服务；设置一个线程为守护线程应该在线程start之前setDaemon()。

### 4.2.7 线程优先级

java中线程可以有自己的优先级，优先级高的更有优势抢占资源；线程优先级高的不一定能抢占到资源，只是一个概率问题，而对应优先级低的线程可能会发生饥饿；

在java中使用1到10表示线程的优先级，使用setPriority()方法来进行设置，数字越大代表优先级越高；

## 4.3 多线程编程


## 4.3.1 java线程锁的分类与实现

以下分类是从多个同角度来划分，而不是以某一标准来划分，请注意

- 阻塞锁：当一个线程获得锁，其他线程就会被阻塞挂起，直到抢占到锁才继续执行，这样会导致CPU切换上下文，切换上下文对CPU而言是很耗费时间的
- 非阻塞锁：当一个线程获得锁，其他线程直接跳过锁资源相关的代码继续执行，就是非阻塞锁
- 自旋锁：当一个线程获得锁，其他线程则在不停进行空循环，直到抢到锁，这样做的好处是避免了上下文切换
- 可重入锁：也叫做递归锁，当一个线程外层函数获得锁之后 ，内层递归函数仍然可以该锁的相关代码，不受影响。
- 互斥锁：互斥锁保证了某一时刻只能有一个线程占有该资源。
- 读写锁：将代码功能分为读和写，读不互斥，写互斥；
- 公平锁/非公平锁：公平锁就是在等待队列里排最前面的的先获得锁，非公平锁就是谁抢到谁用；
- 重量级锁/轻量级锁/偏向锁：使用操作系统“Mutex Lock”功能来实现锁机制的叫重量级锁，因为这种锁成本高；轻量级锁是对重量级锁的优化，提高性能；偏向锁是对轻量级锁的优化，在无多线程竞争的情况下尽量减少不必要的轻量级锁执行路径。
- 乐观锁
- 悲观锁



### 4.3.2 synchronized

属于阻塞锁，互斥锁，非公平锁，可重入锁，在JDK1.6以前属于重量级锁，后来做了优化；

用法：

- 指定加锁对象；
- 用于静态代码块/方法
- 用于动态代码块/方法

示例

```
		public static synchronized void test1(){
            System.out.println("test");
        }

        public  synchronized void test2(){
            System.out.println("test");
        }
                 
        public void test3(){
            synchronized (this){
                System.out.println("test");
            }
        }
```

当锁加在静态代码块/方法上时，锁作用于整个类，凡是属于这个类的对象的相关都会被上锁，当用于动态代码块/方法/对象时锁作用于对象；除此之外，synchronized可以保证线程的可见性和有序性。

synchronized 的底层原理可以通过操作码来查看，通过持有对象的监视器（monitor）锁来实现 
在操作码中可以看到这两个指令 `monitorenter`  `monitorexit` 一个指令是持有对象监视器，一个指令是释放对象监视器

### Lock

lock 是一个接口，其下有多个实现类；

方法说明：

- lock()方法是平常使用得最多的一个方法，就是用来获取锁。如果锁已被其他线程获取，则进行等待。
- tryLock()方法是有返回值的，它表示用来尝试获取锁，如果获取成功，则返回true，如果获取失败（即锁已被其他线程获取），则返回false，这个方法还可以设置一个获取锁的等待时长，如果时间内获取不到直接返回。
- 两个线程同时通过lock.lockInterruptibly()想获取某个锁时，假若此时线程A获取到了锁，而线程B只有在等待，那么对线程B调用threadB.interrupt()方法能够中断线程B的等待过程
- unLock()方法是用来释放锁
- newCondition()：生成一个和线程绑定的Condition实例，利用该实例我们可以让线程在合适的时候等待，在特定的时候继续执行；相当于得到这个线程的wait和notify方法；

### ReentrantLock

ReentrantLock重入锁，是实现Lock接口的一个类，它对公平锁和非公平锁都支持；在构造方法中传入一个boolean值，true时为公平锁，false时为非公平锁 它是 AQS实现的一个锁

### Semaphore(信号量)

信号量是对锁的扩展，锁每次只允许一个线程访问一个资源，而信号量却可以指定多个线程访问某个资源；信号量的构造函数为

```java
public Semaphore(int permits) {
        sync = new NonfairSync(permits);
    }
public Semaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }
```

第一个方法指定了可使用的线程数，第二个方法的布尔值表示是否为公平锁；

acquire()方法尝试获得一个许可，如果获取不到则等待；tryAcquire()方法尝试获取一个许可，成功返回true，失败返回false，不会阻塞，tryAcquire(int i) 指定等待时间；release()方法释放一个许可。

### ReadWriteLock

读写分离锁， 读写分离锁可以有效的减少锁竞争，读锁是共享锁，可以被多个线程同时获取，写锁是互斥只能被一个线程占有，ReadWriteLock是一个接口，其中readLock()获得读锁，writeLock()获得写锁 其实现类ReentrantReadWriteLock是一个可重入得的读写锁，它支持锁的降级(在获得写锁的情况下可以再持有读锁)，不支持锁的升级（在获得读锁的情况下不能再获得写锁）；读锁和写锁也是互斥的，也就是一个资源要么被上了一个写锁，要么被上了多个读锁，不会发生这个资即被上写锁又被上读锁的情况。

### 闭锁和栅栏
闭锁：一个同步辅助类，在完成一组正在其他线程中执行的操作之前，它允许一个或多个线程一直等待。即，一组线程等待某一事件发生，事件没有发生前，所有线程将阻塞等待；而事件发生后，所有线程将开始执行；闭锁最初处于封闭状态，当事件发生后闭锁将被打开，一旦打开，闭锁将永远处于打开状态。

闭锁CountDownLatch唯一的构造方法CountDownLatch(int count)，当在闭锁上调用countDown()方法时，闭锁的计数器将减1，当闭锁计数器为0时，闭锁将打开，所有线程将通过闭锁开始执行。

栅栏：一个同步辅助类，它允许一组线程互相等待，直到到达某个公共屏障点。利用栅栏，可以使线程相互等待，直到所有线程都到达某一点，然后栅栏将打开，所有线程将通过栅栏继续执行。CyclicBarrier支持一个可选的 Runnable 参数，当线程通过栅栏时，runnable对象将被调用。构造函数CyclicBarrier(int parties, Runnable barrierAction)，当线程在CyclicBarrier对象上调用await()方法时，栅栏的计数器将增加1，当计数器为parties时，栅栏将打开。

闭锁和栅栏是反过来的。它们都是 AQS实现的 通过 AQS的 state参数来计数

### 分布式锁

redis 锁

实现思路 
- incr指令   key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作进行加一，其它用户在执行 INCR 操作进行加一时，如果返回的数大于 1 ，说明这个锁正在被使用当中。

- SETNX 如果 key 不存在，将 key 设置为 value 如果 key 已存在，则 SETNX 不做任何动作
- lua脚本实现 lua能保证原子性

zk锁

通过ZK 的顺序临时节点来上锁 

步骤 获取分布式锁的时候在某节点下创建临时顺序节点，然后所有子节点；如果获取到的子节点中自己创建的节点最小则获取到了锁，否则未获取到锁。如果未获取到锁则监听前一位节点，当它被删除的时候查看自己的节点是否是最小节点... 是则获取到锁（反复操作）。
最后释放锁的时候删除自己的临时节点。

## cas(乐观锁实现)

cas(比较替换)：无锁策略的一种实现方式，过程为获取到变量旧值（每个线程都有一份变量值的副本），和变量目前的新值做比较，如果一样证明变量没被其他线程修改过，这个线程就可以更新这个变量，否则不能更新；通俗的说就是通过不加锁的方式来修改共享资源并同时保证安全性。

使用cas的话对于属性变量不能再用传统的int ,long等；要使用原子类代替原先的数据类型操作，比如AtomicBoolean，AtomicInteger，AtomicInteger等。

### 并发下集合类

并发集合类主要有：

- ConcurrentHashMap：支持多线程的分段哈希表，它通过将整个哈希表分成多段的方式减小锁粒度
- ConcurrentSkipListMap：ConcurrentSkipListMap的底层是通过跳表来实现的。跳表是一个链表，但是通过使用“跳跃式”查找的方式使得插入、读取数据时复杂度变成了O（logn）;
- ConCurrentSkipListSet：参考ConcurrentSkipListMap；
- CopyOnWriteArrayList：是ArrayList 的一个线程安全的变形，其中所有可变操作（添加、设置，等等）都是通过对基础数组进行一次新的复制来实现的; 
- CopyOnWriteArraySet：参考CopyOnWriteArrayList; 
- ConcurrentLinkedQueue：cas实现的非阻塞并发队列;

### 线程池

多线程的设计优点是能很大限度的发挥多核处理器的计算能力，但是，若不控制好线程资源反而会拖累cpu，降低系统性能，这就涉及到了线程的回收复用等一系列问题；而且本身线程的创建和销毁也很耗费资源，因此找到一个合适的方法来提高线程的复用就很必要了。

线程池就是解决这类问题的一个很好的方法：线程池中本身有很多个线程，当需要使用线程的时候拿一个线程出来，当用完则还回去，而不是每次都创建和销毁。在JDK中提供了一套Executor线程池框架，帮助开发人员有效的进行线程控制。

1) Executor使用

获得线程池的方法：

- newFixedThreadPool(int nThreads) ：创建固定数目线程的线程池；
- newCachedThreadPool：创建一个可缓存的线程池，调用execute将重用以前构造的线程（如果线程可用）。如果现有线程没有可用的，则创建一个新线 程并添加到池中；
- newSingleThreadExecutor：创建一个单线程化的Executor；
- newScheduledThreadPool：创建一个支持定时及周期性的任务执行的线程池。

以上方法都是返回一个ExecutorService对象，executorService.execute()传入一个Runnable对象，可执行一个线程任务

下面看示例代码

```java
public class Test implements Runnable{
	int i=0;
	public Test(int i){
		this.i=i;
	}
	public void run() {
		System.out.println(Thread.currentThread().getName()+"====="+i);
	}
    public static void main(String[] args) throws InterruptedException {
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
		for(int i=0;i<10;i++){
			cachedThreadPool.execute(new Test(i));
			Thread.sleep(1000);
		}
	}
}

```

线程池是一个庞大而复杂的体系，本系列文章定位是基础，不对其做更深入的研究，感兴趣的小伙伴可以自行查资料进行

1.1) ScheduledExecutorService

newScheduledThreadPool(int corePoolSize)会返回一个ScheduledExecutorService对象，可以根据时间对线程进行调度；其下有三个执行线程任务的方法：schedule()，scheduleAtFixedRate()，scheduleWithFixedDelay()；该线程池可解决定时任务的问题。

示例：

```java
class Test implements Runnable {
    
    private String testStr;
    
    Test(String testStr) {
        this.testStr = testStr;
    }

    @Override
    public void run() {
        System.out.println(testStr + " >>>> print");
    }
    
    public static void main(String[] args) {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
        long wait = 1;
        long period = 1;
        service.scheduleAtFixedRate(new MyScheduledExecutor("job1"), wait, period, TimeUnit.SECONDS);
        service.scheduleWithFixedDelay(new MyScheduledExecutor("job2"), wait, period, TimeUnit.SECONDS);
        scheduledExecutorService.schedule(new MyScheduledExecutor("job3"), wait, TimeUnit.SECONDS);//延时waits 执行
    }
}
```

job1的执行方式是任务发起后间隔`wait`秒开始执行，每隔`period`秒(注意：不包括上一个线程的执行时间)执行一次；

job2的执行方式是任务发起后间隔`wait`秒开始执行，等线程结束后隔`period`秒开始执行下一个线程；

job3只执行一次，延迟`wait`秒执行；

ScheduledExecutorService还可以配合Callable使用来回调获得线程执行结果，还可以取消队列中的执行任务等操作，这属于比较复杂的用法，我们这里掌握基本的即可，到实际遇到相应的问题时我们在现学现用，节省学习成本。

### ThreadPoolExecutor 

```
corePoolSize： 线程池维护线程的最少数量 （core : 核心）
maximumPoolSize： 线程池维护线程的最大数量
keepAliveTime：线程池维护线程所允许的空闲时间
unit：  线程池维护线程所允许的空闲时间的单位
workQueue：线程池所使用的缓冲队列
handler：线程池对拒绝任务的处理策略
```

## 4.4 AQS
指 AbstractQueuedSynchronizer 
ReentrantLock、Semaphore、CountDownLatch、CyclicBarrier等并发类均是基于AQS来实现的

private volatile int state 属性

ReentrantLock用它来计数重入锁次数 Semaphore，CountDownLatch，CyclicBarrier等并发类均是基于AQS来实现的 用它来计数线程数

static final class Node

AQS内部类 实现CLH队列(FIFO) 它的waitStatus 表示线程状态

CANCELED= 1 // 线程已被取消

SIGNAL= -1 // 表示后继节点需要被唤醒

CONDITION= -2 // 表示该节点在条件队列等待

PROPAGATE= -3 // 表示下个共享节点aquire时无条件的传播

# 5. zookeeper
`zookeeper`对分布式系统来说是一个很重要必须要掌握的中间件，对于ZK的安装部署这里就不做介绍了，自行百度，主要讲使用和部分原理。
## 5.1 ZK介绍
`zookeeper`是基于观察者模式设计的分布式服务管理框架，它负责存储和管理比较重要的分布式数据并通知观察者数据的变化状态，直白的说zookeeper是一个数据存储加消息通知系统。zookeeper的应用场景有:

- 统一命名服务：在分布式系统中给每个应用配置一个全局唯一名称，并统一管理
- 统一配置管理：将分布式系统一些配置信息放入到ZK中进行管理
- 统一集群管理：管理监听集群状态
- 服务节点动态上下线：实时通知应用分布式系统中有哪些服务节点。

zk的特性：
- 顺序一致性： 从同一客户端发起的事务请求，最终将会严格地按照顺序被应用到 ZooKeeper 中去。
- 原子性： 所有事务请求的处理结果在整个集群中所有机器上的应用情况是一致的，也就是说，要么整个集群中所有的机器都成功应用了某一个事务，要么都没有应用。
- 单一系统映像 ： 无论客户端连到哪一个 ZooKeeper 服务器上，其看到的服务端数据模型都是一致的。
- 可靠性： 一旦一次更改请求被应用，更改的结果就会被持久化，直到被下一次更改覆盖。

## 5.2 ZNode

zookeeper的数据结构整体上一棵树，每个节点被称作`ZNode`，每个ZNode默认存储1MB的数据，每个ZNode 都可以通过路径唯一标识。ZNode共有四种类型：
- 持久节点：指在节点创建后，就一直存在，直到有删除操作来主动清除这个节点。不会因为客户端会话失效而清除；
- 持久顺序节点：在持久节点基础上增加了有序性，其每创建一个子节点都会自动为给节点名加上一个数字后缀作为新的节点名。

- 临时节点：临时节点的生命周期和客户端会话绑定。也就是说，如果客户端会话失效，那么这个节点就会自动被清除掉。
- 临时顺序节点：在临时节点基础上增加了有序性；参考持久顺序节点。

## 5.3 ZK指令
在ZK的安装包中有一个ZK客户端，启动ZK客户端可在其中输入相应的指令来操作ZK，下面对这些指令做简单介绍：

| 指令              | 描述                                                         |
| ----------------- | ------------------------------------------------------------ |
| help              | 显示所有操作命令                                             |
| ls path [watch]   | 查看当前节点内容                                             |
| ls2  path [watch] | 查看当前节点数据并能看到更新次数等数据                       |
| create            | 不带参数创建普通持久节点，-s 创建持久顺序节点 -e 创建临时节点，-s -e 创建 临时顺序节点 |
| get path [wathc]  | 获取节点值                                                   |
| set path          | 给节点赋值                                                   |
| stat path         | 查看节点状态                                                 |
| delete path       | 删除节点                                                     |
| rmr               | 递归删除节点 (参考rm-rf）                                    |
操作示例：
```shell
# 连接zk
./zkCli.sh -server master 2181

# 列出 / 下的节点
ls /

# 创建节点
create /zk-test "123"
create  -s   /zk-test  “test123”
create -e /zk-test123 "test1234"

# 删除节点
delete /zk-test

# 获取节点
get /zk-123

#更新节点
set  /zk-123 "d"

```
## 5.3 ZK配置文件
示例：
```shell

tickTime=2000
dataDir=E:/zookeeper/zookeeper-3.4.8 - colony/zookeeper-1/tmp/zookeeper/
clientPort=2181
initLimit=10
syncLimit=5
server.1=127.0.0.1:2888:3888
server.2=127.0.0.1:2889:3889
server.3=127.0.0.1:2890:3890
```
配置项说明：
简单列举，详细参考 http://www.aboutyun.com/forum.php?mod=viewthread&tid=13909

- clientPort: 客户端连接server的端口，即zk对外服务端口，一般设置为2181。
- dataDir : 把内存中的数据存储成快照文件snapshot的目录
- tickTime: ZK中的一个时间单元
- syncLimit: 如果Leader发出心跳包在syncLimit之后，还没有从Follower那里收到响应，那么就认为这个Follower已经不在线了。


## 5.4 ZK机制
### 5.4.1 Zookeeper工作原理
Zab协议 的全称是 Zookeeper Atomic Broadcast （Zookeeper原子广播）。ZAB协议定义了 选举（election）、发现（discovery）、同步（sync）、广播(Broadcast) 四个阶段；
选举阶段就是选举出leader。发现阶段follower节点向准leader推送自己的信息，接受准leader的newEpoch指令，检查newEpoch有效性,如果校验没有问题则正式进入一个新的leader统治时期（epoch）。同步阶段将Follower与Leader的数据进行同步，由Leader发起同步指令，最终保持集群数据的一致性；广播阶段，leader发起广播，Follower开始提交事务。

为了保证事务的顺序一致性，zookeeper采用了递增的事务id号（zxid）来标识事务。所有的提议（proposal）都在被提出的时候加上了zxid。zxid是一个64位的数字，它高32位用来标识leader关系是否改变，每次一个leader被选出来，它都会有一个新的标识，代表当前leader，低32位用于递增计数。
在ZK集群中，Server有三种状态： 
- LOOKING：当前Server不知道leader是谁，正在搜寻
- LEADING：当前Server即为选举出来的leader
- FOLLOWING：leader已经选举出来，当前Server与之同步

当ZK的server挂掉半数以上，leader就认为集群不能再正常工作了；所以ZK集群一般为奇数个。 

### 5.4.2 ZK选主流程
ZK集群中每个Server启动，首先会投自己一票，然后向外对其他ZK发送报文，如果有响应则互相交换投票结果，如果结果无法确定leader是谁则继续投票。投票规则是优先投票给id最大的server，且不能重复投某个server。因此一个server若想做leader，它的id要足够大（通过配置文件配置），而且还有尽快和其他server建立通讯。


### 5.4.3 Broadcast(广播)
当客户端提交事务请求时Leader节点为每一个请求生成一个Proposal(提案)，将其发送给集群中所有的Follower节点，收到过半Follower的反馈后开始对事务进行提交；只需要得到过半的Follower节点反馈Ack（同意）就可以对事务进行提交；过半的Follower节点反馈Ack 后，leader发送commit消息同时自身也会完成事务提交，Follower 接收到 commit 消息后，会将事务提交。

Follower必须保证事务的顺序一致性的，也就是说先被发送的Proposal必须先被；消息广播使用了TCP协议进行通讯所有保证了接受和发送事务的顺序性。广播消息时Leader节点为每个Proposal分配一个全局递增的ZXID（事务ID），每个Proposal都按照ZXID顺序来处理。

如果我们连接上某个zk发送一个写请求，如果这个zk不是Leader，那么它会把接受到的请求进一步转发给Leader，然后leader就会执行上面的广播过程。而其他的zk就能同步写数据，保证数据一致。


## 5.5 ZK面试问题

- 脑裂：由于心跳超时（网络原因导致的）认为master死了，但其实master还存活着（假死），假死会发起新的master选举，选举出一个新的master。但是客户端还能和旧的master通信，导致一部分客户端连接旧master（直连）,一部分客户端连接新的master
- znode类型：临时无序，临时有序，持久无序，持久有序
- Zookeeper通知机制：client端会对某个znode建立一个watcher事件，当该znode发生变化时，这些client会收到zk的通知，然后client可以根据znode变化来做出业务上的改变等。
- 概述zk 工作原理：Zookeeper 的核心是原子广播，这个机制保证了各个Server之间的同步。实现这个机制的协议叫做Zab协议。Zab协议有两种模式，它们分别是恢复模式（选主）和广播模式（同步）。当服务启动或者在领导者崩溃后，Zab就进入了恢复模式，当领导者被选举出来，且大多数Server完成了和 leader的状态同步以后，恢复模式就结束了。状态同步保证了leader和Server具有相同的系统状态。

# 6. dubbo
支持的协议 实现rmi 规范 

http redis dubbo hessian dubbo rmi webservice thrift memcached redis

dubbo协议只适合小量数据传输

配置 优先级

启动时配置 >  xml配置> properties配置

- 启动时检查

检查消费者是否已经在注册中心里面 启动失败 dubbo.reference check=false dubbo.consumer 注册中心启动时检查

- 超时设置

@reference timeout属性 超时默认值1000 粒度可控制到方法级别 高精度优先 消费者优先

- 重试次数

@reference retries 轮流重试服务 重试幂等

- 多版本控制

@service version @reference version version=“*” 任意版本

- 本地存根

在服务消费方提供存根实现，提供有参构造器 属性为远程接口调用远程方法失败调用本地存根

包扫描

@enabledubbo dubbo.scan.base-package

- 高可用

消费者缓存服务提供者信息和dubbo直连


- 负载均衡策略（服务端和消费端都能配置）

 基于权重的随机负载均衡 

 基于权重的轮训负载均衡 

 最少活跃数负载均衡

 一致性哈希负载均衡



- 服务降级

客户端直接返回为空 调用失败后服务降级 可在dubbo admin 屏蔽消费端的调用 和容错。

- 服务容错

重试 快速失败 失败安全 忽略失败 失败自动恢复 定时调用 并行调用 调用多台机器，只要一个成功就行 广播调用 调用多台 需要全部成功


- 分层

业务逻辑层

rpc层（配置层 注册中心层 负载均衡层 监控层 远程调用层）

remoting 层（信息交换层 transport层  序列化层）

netty 和mina 被封装成统一的接口在 Remoting下的 Transport层





# 7. sharding jdbc

- 公共表（广播表）

公共表属于系统中数据量较小，变动少，而且属于高频联合查询的依赖表。参数表、数据字典表等属于此类型。可以将这类表在每个数据库都保存一份，所有更新操作都同时发送到所有分库执行。sharding jdbc 可通过代码配置来实现公共表

```
# 指定table为公共表
spring.shardingsphere.sharding.broadcast-tables=table
```

- 分片策略

标准分片策略

复合分片策略

行表达式分片 （取模分片可用该方式）

Hint分片策略


- 执行过程是什么样的


1.sql 解析 
过程分为 词法解析和语法解析 词法解析器将sql拆解为不可再分的原子符号 称为token。并根据不同的数据库方言所提供的数据库字典将其归类为 表达式 字面量 操作符。在使用语法解析器将sql转换为抽象树

sql 路由 将逻辑表映射到对应的拆分的表中。根据分片键的不同可以分为单片路由 多片路由 范围路由。如果查询不携带分片键则是广播路由。根据分片键的场景可以分为直接路由 标准路由 笛卡尔路由。

sql改写

将sql 拆分成多个sql 根据路由情况去改写sql，补充结果归并所需要的列如，示例：

```sql
# 原始sql
select a from table where b>10 order by c

# 改写 加了c列 因为结果归并的时候需要这个列
select a,c from table_0 where b>10;

select a,c from table_1 where b>10;
```

sql执行

 sql 执行引擎执行sql（并发执行）,自动平衡数据源连接资源和内存资源。它有两个模式 内存限制模式和连接限制模式。

结果归并

将查询结果归并为一个结果集，由归并引擎来完成。

结果归并从功能上划分为遍历 排序 分组  分页 聚合 

结果归并的方式有 内存归并 流式归并 装饰者归并；

流式归并的 边组装结果集边归并（主要方案 重点）。



- 分片策略根据业务场景去制定的

如销售单下有多个商品 这个商品数据就可以根据销售单的id 去做分片 确保一个销售单下的商品在一张表中

- 联表查询

如果联表条件不是分片键则会产生迪卡尔路由，会去联表查询所有的数据节点。



# 8. sentinel

# 9. rabbitMQ
## 9.1 rabbitMQ简介
`rabbitMQ`是基于`AMQP`协议的消息中间件，即`Advanced Message Queuing Protocol`，高级消息队列协议，是应用层协议的一个开放标准，为面向消息的中间件设计。消息中间件主要用于组件之间的解耦，消息的发送者无需知道消息使用者的存在，反之亦然。 AMQP的主要特征是面向消息、队列、路由（包括点对点和发布/订阅）。基于AMQP协议的消息中间件还有`kafka`，`rocketMQ`等。还有另外一类基于jms协议的消息中间件（如activeMQ），这类MQ相对AMQP来说扩展性有所不足，因此大部分公司都会选择AMQP产品。而rabbitMQ以其安装部署简单，上手门槛低，功能丰富，集群易扩展，有强大的WEB管理页面，消息可靠投递机制等优点受广大开发人员欢迎。

介绍了这么多，或许有的小伙伴还是不太明白MQ的使用场景，MQ作用就六个字——异步，削峰，解耦。它在大型电子商务类网站，如京东、淘宝、去哪儿等网站有着深入的应用。比如一个下单流程，在不使用消息队列的情况下，用户的请求数据直接写入数据库，在高并发的情况下，会对数据库造成巨大的压力，同时也使得系统响应延迟加剧。在使用队列后，用户的请求发给队列后立即返回，后续处理交给队列，在业务逻辑上只需要做简单修改即可（如告诉用户系统确认订单中，等MQ处理完再更新订单状态），这只是一个简单的使用场景，MQ的用武之地还很多。
## 9.2 rabbitMQ成员角色
学习rabbtMQ我们先要弄清楚这几个概念：`exchange`,`queue`,`routing-key`,`binding-key`,`message`,`publisher`,`exchange`,`binding-key`,`Connection`,`Channel`,`consumer`,`broker`；下面对这些角色概念进行介绍。

消息的发送方被称作`publisher`（生产者），而消息的接收方被称作`consumer`(消费者)，而消息队列服务器实体就是`broker`（指`rabbitMQ`）；消费者或者生产者对rabbitMQ的一个连接被称作`Connection`（连接）,在rabbit的连接模型中，为了提高连接传输效率，采用了`Channel`（管道）这种方式实现多路复用，类似于Nio中的模型；我们知道建立一个TCP连接代价很大，因此TCP连接建立后最好不要断开`Connection`-`Channel`连接模型就是为了达到这种目的；一个消费者（生产者）使用一个`channel`消费（发送）消息，而多个`Channel`共用一个`Connection`。

一个生产者向rabbit投递消息，然后消费者消费这个消息的过程是这样的——生产者将消息投递给rabbit，在rabbit中`exchange`（交换机）首先会接收到这个消息，交换机相当于一个“分拣员”的角色，负责分拣消息，将这些消息存储到和自己绑定的`queue`（队列）中去，然后和队列绑定的消费者会消费这些消息。队列和交换机绑定通过一个`binding-key`（绑定键）来标记，而生产者投递消息给交换机的时候会指定一个`routing-key`（路由键），而交换机会根据路由和绑定键来判断将消息放到那些队列中去（扩展：kafka）。

在rabbit中交换机共有四种类型，下面对其类型和其消息路由规则做说明：
- `direct exchange`(直连交换机)：消息中的`routing-key`如果和`binding-key`一致， 交换器就将消息发到对应的队列中,`routing-key`要与`binding-key`完全匹配。
- `fanout exchange`(扇型交换机):扇型交换机会将交给自己的消息发到所有和自己绑定的队列中去，它不会去匹配`routing-key`和`binding-key`。
- `topic exchange`(主题交换机):主题交换机的`routing-key`匹配`binding-key`的方式支持模糊匹配， 以.分割单词，`*`匹配一个单词，`#`匹配多个单词，比如如路由键是`com.muggle.first` 能被`com.#`和`*.muggle.*`绑定键匹配。
- `headers exchange`(头交换机):类似主题交换机，但是头交换机使用多个消息属性来代替路由键建立路由规则。通过判断消息头的值能否与指定的绑定相匹配来确立路由规则。当交换机的`x-match`属性为`any`时，消息头的任意一个值被匹配就可以满足条件,当为`all`的时候，就需要消息头的所有值都匹配成功,这种交换机在实际生产中用的并不多。

## 9.3 springboot+rabbitMQ使用案例
本文的demo已经放到github上去了，有需要的小伙伴可以去拉下来（顺便求个star），地址： 。运行本项目之前请先安装好`rabbitMQ`，用`docker`安装的话，一个命令就搞定了，如果安装在windows上需要注意这几个坑——1.确保`Erlang`安装成功（安装过程中会提示），2.rabbitMQ的web管理插件需要另外安装，3. 注意用户权限配置。小伙伴可以参考这篇博客：`https://www.cnblogs.com/lykbk/p/erewererewr32434343.html`。这一节主要介绍怎么在springboot中集成使用rabbitMQ。后续章节会介绍，交换机、队列、消息等角色相关参数如何在项目中配置使用。

在正式使用前我们先来瞅瞅rabbitMQ的web管理界面，访问`http://localhost:15672/#/`登录后，你会看到`图1`这些东西：`Overview` 是对rabbitMQ整体情况统计的界面，`Connections`是对连接进行管理的界面，`Queues`是队列管理界面，`Admin`是用户管理界面，以此类推。

### 9.3.1 简单消息队列
springboot会默认为你创建一个`direct exchange`类型交换机，其名称为`""`空字符串，其路由键和绑定键都是队列名称，未指定交换机的队列都会绑定到这个交换机上去。我们就以这个最简单的消息队列开始来学习如何在项目中使用`rabbitMQ`。依赖如下：
```xml
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
 </dependency>
 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

 <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
 </dependency>

 <dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-text</artifactId>
    <version>1.2</version>
 </dependency>

 <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
 </dependency>
```
properties:
```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.username=guest
spring.activemq.password=guest
```
注册两个交换机，一个用于传递String类型消息，一个传递Object类型的数据。项目启动后springboot会为你在 rabbitMQ 中创建两个队列，启动项目后打开 rabbitMQ 的 web 管理界面（以下简称管理界面）会在 Queues 中看到这两个队列的相关信息。
```java
@Component
public class QueueConfig {
    @Bean
    public Queue getSimpleQueue() {
        return new Queue("simple-queue");
    }

    @Bean
    public Queue getObjSimpleQueue() {
        return new Queue("obj-simple-queue");
    }
}
```
创建两个定时任务，向 rabbitMQ 投递消息，注意这里需要在启动类上加 `@EnableScheduling` 注解以启动定时任务，而 `Message` 是我创建的实体类：
```java
@Component
public class ScheduleHandler {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Scheduled(fixedRate = 6000)
    private void simpleQueueSchedule() {
        System.out.println("<<<<<<<<<<");

        rabbitTemplate.convertAndSend("simple-queue","ni----hao");
    }

    @Scheduled(fixedRate = 6000)
    private void objSimpleQueueSchedule() {
        System.out.println("<<<<<<<<<<");
        Message message = new Message();
        message.setTitle("hello");
        message.setContent("how are you ");
        rabbitTemplate.convertAndSend("obj-simple-queue",message);
    }

}
```
消费者消费消息：
```java
@Component
public class QueueMessageHandler {

    @RabbitListener(queues = { "simple-queue"})
    public void getSimpleQueueMessage(String msg){
        System.out.println(msg);
    }

    @RabbitListener(queues = { "obj-simple-queue"})
    public void getObjSimpleQueueMessage(Message msg){
        System.out.println(msg);
    }

}
```

`rabbitTemplate.convertAndSend()`方法是将数据序列化并写入队列中，而其使用的序列化协议自然是java序列化协议（使用 `ObjectInputStream` 和 `ObjectOutputStream` 读写），因此你如果调用这个方法则其实体类需要实现`Serializable`接口，而如果跨虚拟机还需要注意 `serialVersionUID`。如果跨平台了，那么最好使用其他序列化的方式,序列化反序列化配置在后文介绍。

### 9.3.2 推模式和拉模式
对消费端而言使用`@RabbitListener`监听器获取MQ消息的方式称为`推模式`，我们还可以使用拉模式，当我们需要一条消息的时候才从队列中拉一条消息出来，使用的方法为 `rabbitTemplate.receiveAndConvert()`，如：
```
  Message o = ((Message) rabbitTemplate.receiveAndConvert("obj-simple-queue"));
```

### 9.3.3 direct exchange 直连交换机
直连交换机，需要注册一个 `DirectExchange` , `Queue` , `Binding` 。`Bingding` 负责将 `DirectExchange` 和 `Queue` 绑定并指定 `routingKey` 生产者生产消息的时候也需要指定 `routingKey`。下面看示例：
```java
//  生产端配置
    @Bean("directQueueFirst")
    public Queue directQueueFirst() {
        return new Queue("first-direct-queue");
    }

    @Bean("directQueueSecond")
    public Queue directQueueSecond() {
        return QueueBuilder.durable("second-direct-queue").build();
    }
    @Bean("directExchange")
    public DirectExchange directExchange() {
        return new DirectExchange("direct-exchange");
    }
    
    @Bean
    public Binding bingQueueFirstToDirect(@Qualifier("directQueueFirst") Queue queue, @Qualifier("directExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("first-key");
    }

    @Bean
    public Binding bingQueueSecondToDirect(@Qualifier("directQueueSecond") Queue queue, @Qualifier("directExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("second-key");
    }
    
//  生产者发送消息
@Component
public class ScheduleHandler {

    @Scheduled(fixedRate = 6000)
    private void directMessageScheduleFirst() {
        Message message = new Message();
        message.setTitle("hello");
        message.setContent("how are you for direct first");
        rabbitTemplate.convertAndSend("direct-exchange","first-key",message);
    }

    @Scheduled(fixedRate = 6000)
    private void directMessageScheduleSecond() {
        Message message = new Message();
        message.setTitle("hello");
        message.setContent("how are you for direct second");
        rabbitTemplate.convertAndSend("topic-exchange","second-key",message);
    }
}
@Component
public class QueueMessageHandler {
//  消费端
    @RabbitListener(queues = { "first-direct-queue"})
    public void firstDirectMessageQueue(Message msg){
        System.out.println(msg);
    }

    @RabbitListener(queues = { "second-direct-queue"})
    public void secondDirectMessageQueue(Message msg){
        System.out.println(msg);
    }
}
```
值得注意的是，springboot为了使我们的代码可读性更好，还非常贴心的提供 `Exchange`,`Binding`,`Queue`的`Builder`（建造者），因此你可以使用它们对应建造者，也可以使用直接 new 的方式进行创建。另外创建的这些 exchange queue 都能在管理界面上看到，如图 2 ，图 3 ：

### fanout exchange 扇型交换机
使用上和 direct exchange 大同小异，只不过不需要指定路由键，而且所有和它绑定的队列都会收到消息，直接上代码：
```java
// 生产者配置
    @Bean("fanoutQueueFirst")
    public Queue fanoutQueueFirst() {
        return new Queue("first-fanout-queue");
    }

    @Bean("fanoutQueueSecond")
    public Queue fanoutQueueSecond() {
        return new Queue("second-fanout-queue");
    }

    @Bean("fanoutExchange")
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("fanout-exchange");
    }

    @Bean
    public Binding bingQueueFirstToExchange(@Qualifier("fanoutQueueFirst") Queue queue, @Qualifier("fanoutExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    public Binding bingQueueSecondToExchange(@Qualifier("fanoutQueueSecond") Queue queue, @Qualifier("fanoutExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }
//  生产者发消息，注意这里虽然填了routingKey 但是是无效的
    @Scheduled(fixedRate = 6000)
    private void directMessageScheduleFirst() {
        Message message = new Message();
        message.setTitle("hello");
        message.setContent("how are you for direct first");
        rabbitTemplate.convertAndSend("direct-exchange","first-key",message);
    }
//  消费者，两个队列都能收到同一份消息

    @RabbitListener(queues = { "first-fanout-queue"})
    public void firstFanoutQueue(Message msg){
        System.out.println(msg);
    }

    @RabbitListener(queues = { "second-fanout-queue"})
    public void secondFanoutQueue(Message msg){
        System.out.println(msg);
    }
```

### 主题交换机  Topic
前文介绍了主题交换机的路由方式，注意我代码中的路由键设置，这里我设置两个`bingding-key` 分别是 `com.muggle.first` 和 `com.#` 我用 `routing-key` 为 `com.muggle.test` 发消息这两个队列都能接收到

```java
    @Bean("topicQueueFirst")
    public Queue topicQueueFirst() {
        return new Queue("first-topic-queue");
    }

    @Bean("topicQueueSecond")
    public Queue topicQueueSecond() {
        return new Queue("second-topic-queue");
    }

    @Bean
    public Binding bindTopicFirst(@Qualifier("topicQueueFirst") Queue queue, @Qualifier("topicExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("com.muggle.first");
    }

    @Bean
    public Binding bindTopicSecond(@Qualifier("topicQueueFirst") Queue queue, @Qualifier("topicExchange") TopicExchange exchange) {
        return BindingBuilder.bind(topicQueueFirst()).to(topicExchange()).with("com.#");
    }
    
    @Scheduled(fixedRate = 6000)
    private void topicMessage() {
        Message message = new Message();
        message.setTitle("hello");
        message.setContent("how are you for topic test");
        rabbitTemplate. convertAndSend("topic-exchange","com.muggle.test",message);
    }
    
    
    @RabbitListener(queues = { "first-topic-queue"})
    public void firstTopicMessageQueue(Message msg){
        System.out.println(msg);
    }

    @RabbitListener(queues = { "second-topic-queue"})
    public void secondTopicMessageQueue(Message msg){
        System.out.println(msg);
    }

```
好了，三种常用交换机的使用已经介绍完毕；有疑问的小伙伴可以在评论区留言探讨。关于队列和交换机的进阶使用技巧，且听下回分解。

# 9.4. rabbitMQ 进阶和 springboot 配置
前文我们介绍了在springboot中rabbitMQ的基本使用，现在进一步介绍rabbit的一些配置。
## 9.4.1 持久化
RabbitMQ通过消息持久化来保证消息的可靠性——为了保证RabbitMQ在退出或者发生异常情况下数据不会丢失，需要将 queue ，exchange 和 Message 都持久化。下面分别介绍它们持久化配置的方式。

对于 queue ，exchange 在创建的时候都会提供一个参数用以设置是否持久化，而如果使用它们对应的建造者而不是new，就能很清晰的看到是怎么指定持久化的：
```java
//  创建 queue 指定为非持久化
    QueueBuilder.nonDurable("xxx").build();
//  指定非持久化
     return QueueBuilder.durable("second-direct-queue").build();
//  durable 为true则是持久化，false非持久化
    ExchangeBuilder.topicExchange("topic").durable(true).build();
```
这里需要注意一个地方，那么你直接在原队列的基础上添加属性是会报错的，它会告诉你队列已经存在。需要你手动打开管理界面把那个队列删除掉，然后重启项目。

你如果将 queue 的持久化标识 durable 设置为true ,则代表是一个持久的队列，那么在服务重启之后，也会存在，因为服务会把持久化的 queue 存放在硬盘上，当服务重启的时候，会重新什么之前被持久化的queue；但是里面的消息是否为持久化还需要看消息是否做了持久化设置。exchange 的持久化和 Queue 一样将交换机保存在磁盘，重启后这个交换机还会存在。

那么消息如何持久化呢？在springboot中需要借助`MessagePostProcessor` 消息加工器对消息进行加工 rabbitMQ 才能知道这个消息是不是要持久化，`MessagePostProcessor`还有其他的很多作用，在后文会介绍。下面看如何进行消息的持久化。
创建`MessagePostProcessor`类：
```JAVA
public class MyMessagePostProcessor implements MessagePostProcessor {
    
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        return message;
    }
}
```
生产者通过`MessagePostProcessor`发送消息：
```java
 @Scheduled(fixedRate = 1000)
    private void sendMessageForDlx() {
        rabbitTemplate.convertAndSend("exchange","routing key","mesage",new MyMessagePostProcessor());
    }
```
消息持久化过程：
>  写入文件前会有一个Buffer,大小为1M,数据在写入文件时，首先会写入到这个Buffer，如果Buffer已满，则会将Buffer写入到文件（未必刷到磁盘）。
>  有个固定的刷盘时间：25ms,也就是不管Buffer满不满，每个25ms，Buffer里的数据及未刷新到磁盘的文件内容必定会刷到磁盘。
>  每次消息写入后，如果没有后续写入请求，则会直接将已写入的消息刷到磁盘：使用Erlang的receive x after 0实现，只要进程的信箱里没有消息，则产生一个timeout消息，而timeout会触发刷盘操作。
>  原文链接：https://blog.csdn.net/u013256816/article/details/60875666

## TTL
RabbitMQ可以对消息和队列设置TTL(消息的过期时间)，消息在队列的生存时间一旦超过设置的TTL值，就称为dead message， 消费者将无法再收到该消息。
### 在队列上设置消息过期时间
设置队列过期加一个参数 `x-message-ttl` 就可以搞定，同样记得先把原队列在管理界面删除再启动项目，才会创建队列成功。创建持久化队列：
```java
    Queue build = QueueBuilder.durable("queue")
//      消息过期的时间
                .withArgument("x-message-ttl",5000L).build();
```
这种方式设置的过期属性特性是一旦消息过期，就会从队列中抹去（及时性）。

### 通过`MessagePostProcessor`设置消息过期时间

把原来的 `MyMessagePostProcessor` 代码拿过来加一个参数就行了：
```java
public class MyMessagePostProcessor implements MessagePostProcessor {
    private String expirTime;

    public MyMessagePostProcessor(String expirTime){
        this.expirTime=expirTime;
    }
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
//        设置过期时间
        message.getMessageProperties().setExpiration(expirTime);
//        设置消息持久化
        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        return message;
    }
```
这种方式设置的过期时间即使消息过期，也不一定会马上从队列中抹去，它会等轮到这个消息即将投递到消费者之前进行判定。如果过期就丢弃，不再投递给消费者

## 优先级
优先级分为消息优先级和队列优先级，队列优先级高的会先被处理，消息优先级高的会先被消费，队列优先级配置参数为`x-max-priority`,配置方式为：
```java
Queue build = QueueBuilder.durable("queue").withArgument("x-max-priority",10)
```
配置的数字越大，优先级越高默认优先级为0，消息优先级设置也一样。消息的优先级还是通过 `MessagePostProcessor` 来设置：
```java
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setPriority(5);
        return message;
    }
```

### 死信队列
通过参数`x-dead-letter-exchange`将一个队列设置为死信队列。死信队列的机制是，如果一条消息成为死信 `dead message`，它不是直接丢弃掉，而是在转发到另外一个交换机，由这个交换机来处理这条死信。利用这一机制可达到消息延时的效果——先注册一个没有消费者且设置了过期时间的队列死信队列，投递给这个队列的消息因为没有消费者过一段时间后就会过期成为死信，过期的死信转发到对应的死信交换机里面去分配给其他队列去处理这些消息。上代码：
```java
//  注册死信队列
    @Bean("dlxQueue")
    public Queue dlxQueue(){
//        new Queue("text",true, false, false,new HashMap<>())
//        x-dead-letter-exchange声明了队列里的死信转发到的交换机名称
        Queue build = QueueBuilder.durable("dlx-queue").withArgument("x-dead-letter-exchange", "gc-exchange")
//                dead letter携带的routing-key
                .withArgument("x-dead-letter-routing-key", "dlx-key")
//                消息在过期的时间
                .withArgument("x-message-ttl",5000L).build();
        return build;
    }
//  队列的交换机    
    @Bean("dlxExchange")
    public DirectExchange  dlxExchange(){
//        ExchangeBuilder.topicExchange().durable()
        return new DirectExchange("dlx-exchange");
    }
//  真正处理消息的队列
    @Bean("gcQueue")
    public Queue gcQueue(){
        Queue build = QueueBuilder.durable("gc-queue").build();
        return build;
    }
//  略
    @Bean("dlxExchange")
    public DirectExchange  dlxExchange(){
//        ExchangeBuilder.topicExchange().durable()
        return new DirectExchange("dlx-exchange");
    }

    @Bean("gcExchange")
    public DirectExchange  gcExchange(){
        return new DirectExchange("gc-exchange");
    }

    @Bean
    public Binding bindingGcQueue(@Qualifier("gcQueue") Queue queue,@Qualifier("gcExchange")DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("dlx-key");
    }

    @Bean
    public Binding bindingDlxQueue(@Qualifier("dlxQueue") Queue queue,@Qualifier("dlxExchange")DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("test-dlx");
    }
```
队列和交换机都注册好了，然后我们分别向 `dlx-queue` 分配一个生产者，向 `gc-queue` 分配一个消费者：
```java
 @Scheduled(fixedRate = 1000)
    private void sendMessageForDlx() {
        rabbitTemplate.convertAndSend("dlx-exchange","test-dlx","test");
    }
    
    @RabbitListener(queues = { "gc-queue"})
    public void gcMessage(String message){
        System.out.println(message);
    }
```
打开管理界面界面你能看到消息的流转过程`dlx-queue`被写入消息，而 `gc-queue` 却没有消息,然后 `dlx-queue` 消息减少而`gc-queue` 消息增多。最终消息在`gc-queue` 被消费。

### 生产者确认机制

假如我们将消息投递给交换机，而交换机路由不到队列该怎么处理呢？在 springboot 中 如果交换机找不到队列默认是直接丢弃，如果我们想保证消息百分百投递该怎么办呢？我们可以这样配置，将 `mandatory` 参数设为 true：

```proper
spring.rabbitmq.template.mandatory=true

```

这个参数的作用是：如果消息路由不到队列中去则退还给生产者。我们也可以通过另外两个参数来设置，效果一样：

```properties
spring.rabbitmq.publisher-returns=true
spring.rabbitmq.publisher-confirms=true
```

开启 `publisher-confirms` 和 `publisher-returns` 这两个参数或者 `mandatory` 参数开启的是 生产者的两个监听器 的回调函数 `ConfirmCallback` 和 `ReturnCallback` 。`ConfirmCallback`是在消息发给交换机时被回调，通过这个回调函数我们能知道发送的消息内容，路由键，交换机名称，是否投递成功等内容；而 `ReturnCallback` 则是在交换机路由不到队列的时候被调用。它通过这个回调函数将你的消息退还给你，让你自行处理。上代码：

```java
@Component
public class MyConfirmCallback implements RabbitTemplate.ConfirmCallback {
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        System.out.println("消息唯一标识："+correlationData);
        System.out.println("确认结果："+b);
        System.out.println("失败原因："+s);
    }
}

@Component
public class MyReturnCallback implements RabbitTemplate.ReturnCallback {

    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息主体 message : "+message);
        System.out.println("消息主体 message : "+replyCode);
        System.out.println("描述："+replyText);
        System.out.println("消息使用的交换器 exchange : "+exchange);
        System.out.println("消息使用的路由键 routing : "+routingKey);
    }

}

@Component
@Order(1)
public class RabbitConfig {
    @Autowired
    public RabbitConfig( RabbitTemplate rabbitTemplate,MyConfirmCallback 		confirmCallback,MyReturnCallback returnCallback){
        rabbitTemplate.setReturnCallback(returnCallback);
        rabbitTemplate.setConfirmCallback(confirmCallback);
    }
}

@Component
@Order(5)
public class ScheduleHandler {
    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Scheduled(fixedRate = 6000)
    private void simpleQueueSchedule() {
        System.out.println("<<<<<<<<<<");
        rabbitTemplate.convertAndSend("null-queue","ni----hao");
    }
}
```

配置好之后我们把消息投递给一个不存在的队列 `null-queue` ，你就会看到两个回调函数依次被触发。通过这个机制，生产者就可以确认消息是否被成功投递。在 rabbit 3.0 版本以前还有一个 `immediate` 参数来保证消息所在队列中有消费者，后来被取消。

### 消费者确认机制

在拉模式下，消费者主动去一条消息，不存在确认问题；而推模式下消费者是被动接收消息的，那么如果消费者不想消费这条消息该怎么办呢，rabbit 提供了消费端确认机制，在 springboot 中消费端确认默认是 `NONE` 自动确认，我们需要设置成手动确认 `manual` 或者根据情况确认 `AUTO` 才能使用这一功能：

```properties
# 这里的配置是指向容器 SimpleMessageListenerContainer和DirectMessageListenerContainer 后文会介绍
# spring.rabbitmq.listener.simple.acknowledge-mode=auto
spring.rabbitmq.listener.direct.acknowledge-mode=auto
```

改造消费者：

```java
    @RabbitListener(queues = { "obj-simple-queue"})
    public void testCallBack(Message msg,Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long tag){
        try {
            // 做些啥
          if (xxx){
                channel.basicAck(tag,false);
            }else {
                channel.basicNack(tag,false,true);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(msg);
    }
```

采用消息确认机制后，消费者就有足够的时间处理消息(任务)，不用担心处理消息过程中消费者进程挂掉后消息丢失的问题，因为RabbitMQ会一直持有消息直到消费者显式调用 `basicAck`  为止。如果 `RabbitMQ` 没有收到回执并检测到消费者的 rabbit 连接断开，则  rabbit  会将该消息发送给其他消费者进行处理。一个消费者处理消息时间再长也不会导致该消息被发送给其他消费者，除非它的RabbitMQ连接断开。

在代码中有一个参数 `DELIVERY_TAG` 这个参数是投递的标识；当一个消费者向 rabbit 注册后，会建立起一个 `channel` 当 rabbit 向这个 `channel` 投递消息的时候，会附带一个一个单调递增的正整数 `DELIVERY_TAG`，用于标识这是经过 `channel` 的第几条消息，它的范围仅限于该 `channle`。

下面看一下消费者确认和拒绝消息的方法：

```java
void basicNack(long deliveryTag, boolean multiple, boolean requeue)throws IOException;
void basicReject(long deliveryTag, boolean requeue) throws IOException;
void basicAck(long deliveryTag, boolean multiple) throws IOException;
```

`multiple`：为了减少网络流量，手动确认可以被批处理，当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的channel中缓存的所有消息。`requeue`：消息被拒绝后是否重新进入队列重发。

当 rabbit 队列拥有多个消费者的时候，**队列收到的消息将以轮训的的方式分发到各个消费者**，每条消息只会发送到订阅列表里的一个消费者。这样的会导致一个问题当前一个消费者迟迟不能确认消息的时候，那么下一个消费者只能等。为了解决这个问题，rabbit中 channel 可持有多个未确认消息。可通过配置来指定channel缓存的未确定消息的个数

```java
spring.rabbitmq.listener.simple.prefetch=3
```



消费者的其他相关配置：

```properties
# 消费者端的重试 这里重试不是重发，而是对channel中的消息无法交给监听方法，或者监听方法抛出异常则进行重试，是发生在消费者内部的
spring.rabbitmq.listener.simple.retry.enabled=true
# 初次尝试的时间间隔
spring.rabbitmq.listener..simple.retry.initial-interval=1000 
# 最大重试次数
spring.rabbitmq.listener.simple.retry.max-attempts=3 
#重试时间间隔。
spring.rabbitmq.listener.simple.retry.max-interval=10000 
# 下次重试时间比上次重试时间的倍数
spring.rabbitmq.listener.simple.retry.multiplier=1.0 
# 重试是无状态的还是有状态的。
spring.rabbitmq.listener.simple.retry.stateless=true 

# 并发的消费者最小数量 这里指某一时刻所有消费者并发数量（但似乎最小值没有意义啊）
spring.rabbitmq.listener.concurrency=10
# 并发的消费者最大数量
spring.rabbitmq.listener.max-concurrency=20
```


###  `ListenerContainer ` 的使用

在消费端，我们的消费监听器是运行在 监听器容器之中的（ `ListenerContainer` ），springboot 给我们提供了两个监听器容器 `SimpleMessageListenerContainer` 和 `DirectMessageListenerContainer ` 在配置文件中凡是以 `spring.rabbitmq.listener.simple` 开头的就是对第一个容器的配置，以 `spring.rabbitmq.listener.direct` 开头的是对第二个容器的配置。其实这两个容器类让我很费劲；首先官方文档并没有说哪个是默认的容器，似乎两个都能用；其次，它说这个容器默认是单例模式的，但它又提供了工厂方法，而且我们看 `@RabbitListener` 注解源码：

```java
Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@MessageMapping
@Documented
@Repeatable(RabbitListeners.class)
public @interface RabbitListener {
    String id() default "";
    String containerFactory() default "";
    ......
}
```

它是指定一个 `containerFactory` 那我通过 `@Bean` 注解注册一个 `ListenerContainer ` ` 到底有没有用。

保险起见这里教程中建议注册一个`containerFactory`  而不是一个单例的`ListenerContainer `  那我可以对这个容器工厂做哪些设置呢。它的官方文档`<https://docs.spring.io/spring-amqp/docs/2.1.8.RELEASE/api/>` 其中前往提到的序列化问题就可以配置这个工厂bean来解决：

```java
@Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // 传入一个序列化器 也可以通过 rabbitTemplate.setMessageConverter(）来配置
        factory.setMessageConverter(new MessagingMessageConverter());
        return factory;
    }
```

除此之外 它还能设置事务的长度，消费者并发数，消息重试的相关参数等。小伙伴自己按需查阅资料去进行尝试，这里由于篇幅问题就不做说明了。

### 惰性队列

在 rabbit3.6 版本引入了惰性队列的的概念；默认情况下队列的消息会尽可能的存储在内存之中，这样可以更加快速的将消息发送给消费者，就算持久化的消息也会在内存中做备份。当 rabbit 需要释放内存的时候，会将内存中的消息写入磁盘。这个操作不仅耗时还阻塞队列，让队列无法写入消息。于是 rabbit 将队列分为了两中模式——`default` 模式和 `lazy` 模式来解决这一问题。`lazy` 模式即为惰性队列的模式。惰性队列 通过参数 `x-queue-mode`来配置，代码可参考死信队列，通过  `QueueBuilder` 的 `withArgument` 来指定参数。

惰性队列和普通队列相比，只有很小的内存开销。惰性队列会将消息直接写入到磁盘，需要消费的时候再取出来。当消息量级很大，内存完全不够用的时候，普通队列要经历这样的过程——将消息读到内存 —> 内存满了需要给后面的消息腾地方，将消息写入磁盘—>消费到这条消息，将消息又读入内存。所以当消息量级很大的时候，惰性队列性能要好过普通队列，当内存完全够用的时候则不然。

### 事务

  事务特性是针对生产者投递消息而言的，对我们的项目来说 rabbit 的事务是很重要的；假如没有事务特性，在一个方法中，数据库插入数据失败回滚了，而对应的消息却无法回滚，就会产生一条错误的消息。

rabbit 中的事务机制和 callable 机制是互斥的，也就是说只有 `spring.rabbitmq.template.mandatory=false` 的时候才能使用。rabbit 事务的声明，提交，回滚的方法是channel的 `txSelect()`，`txCoomit()` ，`txRollback()`。但是在 springboot 我们大可不必去手动提交和回滚，可以使用 spring 的声明式事务，上代码：

```java
@Component
@Order(1)
public class RabbitConfig {
    @Autowired
    public RabbitConfig( RabbitTemplate rabbitTemplate,MyConfirmCallback confirmCallback,MyReturnCallback returnCallback){
//        rabbitTemplate.setReturnCallback(returnCallback);
//        rabbitTemplate.setConfirmCallback(confirmCallback);
        // 设置事务环境，使得可以使用RabbitMQ事务
        rabbitTemplate.setChannelTransacted(true);
    }
}
```

生产者：

```java
@Service
public class RabbitTestService {
    @Autowired
    RabbitTemplate template;

    @Transactional(rollbackFor = Exception.class)
    public void test() throws InterruptedException {
        for (int i = 0; i < 30; i++) {

            template.convertAndSend("test for " + i);
            System.out.println(">>>>>" +i);
        }
        Thread.sleep(1000);
        throw new RuntimeException();
        

    }
}
```

通过管理界面和，消费者打印窗口，可确定声明式事务是否配置成功。

### 备胎机

备胎机顾名思义就是替代现任的备胎，“正主” 没了后可以及时上位。在rabbitMQ中，如果生产者发送消息，由于路由错误等原因不能到达指定队列，就会路由到备胎队列消费。这样做可以保证未被路由的消息不会丢失。

备胎交换机的参数为 `alternate-exchange`来指定做谁的备胎：

```java
   @Bean
    public DirectExchange alternateExchange() {
        Map<String, Object> arguments = new HashMap<>();
        //指定做哪个交换机的备胎
        arguments.put("alternate-exchange", "exchange-boss");
        return new DirectExchange("xxxqueue", true, false, arguments);
    }
    @Bean
    public FanoutExchange bossExchange() {
        // 执行业务的交换机
        return new FanoutExchange("exchange-boss");
    }

```
# spring
(面试)
Spring的IOC有三种注入方式 ：构造器注入、setter方法注入、根据注解注入。

BeanFactory

BeanFactory 是spring顶级接口 包含了各种Bean的定义，读取bean配置文档，管理bean的加载、实例化，控制bean的生命周期，维护bean之间的依赖关系。BeanFactroy采用的是延迟加载形式来注入Bean的，即只有在使用到某个Bean时(调用getBean())，才对该Bean进行加载实例化。


ApplicationContext

ApplicationContext实现了BeanFactory ApplicationContext，它是在容器启动时，一次性创建了所有的Bean。

FactoryBean

FactoryBean 实际上是一个bean 当我们把一个实现该接口的bean注册到spring 然后调用Context的getbean方法 的时候并不是返回当前对象而是调用FactoryBean#getObject()来获取一个实例



spring 监听器

实现 ApplicationListener ，继承类ApplicationEvent  创建一个事件类，调用  context.publishEvent 方法发布事件

Spring Bean的生命周期

- 实例化Bean
- 设置对象属性（依赖注入）

- 处理Aware接口 如果这个Bean已经实现了BeanNameAware接口，会调用它实现的setBeanName(String beanId)方法，此处传递的就是Spring配置文件中Bean的id值；如果这个Bean已经实现了BeanFactoryAware接口，会调用它实现的setBeanFactory()方法，传递的是Spring工厂自身。如果这个Bean已经实现了ApplicationContextAware接口，会调用setApplicationContext(ApplicationContext)方法，传入Spring上下文；

- BeanPostProcessor  如果Bean实现了BeanPostProcessor接口，那将会调用postProcessBeforeInitialization(Object obj, String s)方法

- InitializingBean 与 init-method 如果Bean在Spring配置文件中配置了 init-method 属性，则会自动调用其配置的初始化方法。
Spring支持的几种bean的作用域

- PostProcessor 如果这个Bean实现了BeanPostProcessor接口，将会调用postProcessAfterInitialization

- 使用bean 

- DisposableBean 当Bean不再需要时，会经过清理阶段，如果Bean实现了DisposableBean这个接口，会调用其实现的destroy()方法 

- destroy-method 如果这个Bean的Spring配置中配置了destroy-method属性，会自动调用其配置的销毁方法


springmvc 

DispatchServlet 初始化过程 ContextLoaderListener 监听容器初始化然后调用 initWebApplicationContext 创建 WebApplicationContext 通过 WebApplicationContext 初始化组件。

- HandlerMapping (处理器映射器)
- HandlerAdapter （适配器）
- Handler （处理器）
- ViewResolver （视图解析器）
- HandlerExecutionChain (mappedHandler 处理器链)



### EJB 规范

在EJB规范中，将bean归为以下几类Session Beans、Entity Beans、Message Driven Beans 。我们进行应用开发很少用到EJB框架，但是很多东西原理上是相通的；我们编写web应用必定是多线程环境的，而我们采用三层架构模型开发的spring web 应用为什么controller service 是线程安全的，这就涉及到ejb规范中 bean的状态定义。首先我告诉你答案：无状态的bean（方法bean）是线程安全的，有状态的bean（实体bean）是线程不安全的。bean的状态是指有没有实例变量的对象，有没有存储数据能力。在三层架构模型中 单例的service能保证线程安全的一大原因就是他是无状态的bean（方法bean），一旦你再service中定义了一个基本数据类型并使用了它，你就破坏了它的状态，让它变得不再线程安全。这里面的道理想必小伙伴们应该能够想明白。


### jsr

JSR是JavaSpecification Requests的缩写，意思是“Java 规范提案”。虽然大部分java从业者都是做web应用，但他们对j2ee的相关知识却知之甚少，因为 JSR 和spring是有很大关联性的，因此我们在这里顺便科普一下 JSR的相关知识。

Java各种标准的制定是通过Java Community Process （JCP）进行的。JCP的成员可以根据需要提出Java Specification Request （JSR）。每个JSR都要经过提交给JCP，然后JCP讨论，修订，表决通过，并最终定稿。而JavaEE是一组被通过的JSR的合集。

JSR相关内容介绍：

Java Servlet：定义了如何处理Web请求，写过java web 应用的小伙伴应该都知道这个；
JDBC（java Database Connectivity）：JDBC API为访问不同的数据库提供了一种统一的途径，使用java连接过数据库的小伙伴应该对这个不会陌生；
JPA（Java Persistence API）：定义了如何编写ORM和数据存取，如 spring-data-jpa和hibernate框架就是实现了该标准框架；
JTA（Java Transaction Architecture）：定义了如何编写事务相关的代码；
JMS（Java Message Service）：定义了消息队列规范，典型实现就是activeMQ；
CDI（Contexts And Dependency Injection）：定义了如何编写依赖注入；
EJB（Enterprise JavaBean）：定义了javaBean 的相关规范；
JSP(Java Server Pages)：视图模板规范，现在基本上被淘汰了；
JAX（Java API for XML）：xml规范；
RMI（RemoteMethod Invoke）：远程方法调用，区别于RPC（远程过程调用）dubbo就是实现rmi规范的rpc框架；
JavaMail：邮件相关的规范。



# springboot
（面试）
- Spring Actuator 健康健康 
- @ComponentScan 包扫描
- @enableXXX  该类都有一个@Import注解 导入外部配置实现自动化配置
- SpringFactoriesLoader   starter包的类加载器 根据META-INF/spring.factories 加载外部配置
- @conditionXXX  条件注解，自动化配置的开关 

ImportSelector

需要结合@Import注解使用 动态bean注册 ImportSelector接口是spring中导入外部配置的核心接口 selectImports 方法返回需要注册的类的全限定名

ImportBeanDefinitionRegistrar 和ImportSelector 类似 registerBeanDefinitions 方法注册bean

# spring cloud

(面试)
spring cloud context

spring 父容器包 容器名称 Bootstrap Application Context

初始化过程 BootstrapApplicationListener  捕捉spring容器初始化事件 开始初始化父容器，完成后添加一个初始化器AncestorInitializer 给spring 容器
当spring容器调用该初始化器时将bootstrap 绑定为spring容器的父容器


spring cloud common
顶层抽象 EnableDiscoveryClient  ServiceRegistry 负载均衡

eureka 

eureka核心是一个servlet 应用 

eureka缓存  一级 只读缓存为ConcurrentHashMap key为 客户端实例注册的应用名；value 为嵌套的 ConcurrentHashMap

二级读写缓存 key  服务的唯一实例 ID，value 为 Lease 对象，Lease 对象存储着这个实例的所有注册信息，包括 ip 、端口、属性等。

缓存更新的时机 一级缓存 定时器30 更新一次， 从二级缓存拉取。二级缓存 获取缓存时判断缓存中是否没有数据，如果不存在此数据，则通过 CacheLoader 的 load 方法去加载，加载成功之后将数据放入缓存，同时返回数据。

readWriteCacheMap 缓存过期时间，默认为 180 秒，当服务下线、过期、注册、状态变更，都会来清除此缓存中的数据。

Eureka Client 获取全量或者增量的数据时，会先从一级缓存中获取；如果一级缓存中不存在，再从二级缓存中获取；如果二级缓存也不存在，这时候先将存储层的数据同步到缓存中，再从缓存中获取。

自我保护机制

如果在15分钟内超过85%的客户端节点都没有正常的心跳，那么Eureka就认为客户端与注册中心出现了网络故障，Eureka Server自动进入自我保护机制，eureka Server不再从注册列表中移除因为长时间没收到心跳而应该过期的服务。Eureka Server仍然能够接受新服务的注册和查询请求，但是不会被同步到其它节点上，保证当前节点依然可用。

续约

客户端需要发送心跳告知server端自己还活着，防止server端将自己的从registry注册表清除

分布式事务

- saga
- lcn
- tcc
- AP
- XA
# netty
(netty是一门复杂的课程，这里只对面试问题做一部分总结 后期重构)

netty 的编程模型其实和spring 的webflux 是一样的采用的是基于事件驱动的reactor的线程模型 

- reactor 线程模型 面试官可能会问，这里不画图了

- 哪些事件：

inbound 上行事件

通道注册 解除注册 通道活跃事件 通道非活跃事件 异常事件 用户事件 读事件 读完成事件 写状态变化事件 

outbound 下行事件 

端口绑定事件 连接事件 断开连接事件 关闭事件 解除注册事件 刷新事件 读事件 写事件 写出数据刷新事件

- netty 组件

channel eventloop bootstrap channelhandle buff

- netty 的流程说一说 

服务端 serverbootstrap 应用构建的引导类，简化netty应用构建流程 创建父子线程组 设置并绑定channel 注册事件监听处理器

- netty 粘拆包处理

 FixedLengthFrameDecoder 定长拆包器

 LineBasedFrameDecoder 换行符拆包器

 DelimiterBasedFrameDecoder 分隔符拆包器

 LengthFieldBasedFrameDecoder 消息头拆包器


- tcp-ip协议说一说

iso 网络分层 链路层 网络层 传输层 应用层 TCP 协议是 传输层 ip协议是网络层

tcp 如何保证传输的可靠性

一份数据会被切割成多帧，每一帧都包含序号 在tcp的接收端排序组装，去掉重复数据

超时重传，发送方发送数据到接收方，接收方会发送一个一个确认接收的数据帧给接收方，否则会重传

流量控制 当发送方数据发送过快的时候接收方会发送数据帧让发送方降低速度

拥塞控制 通过拥塞窗口来控制数据传输的速率

- http 协议说一说 协议报文包含

请求行 请求首部字段 请求内容实体

响应状态行 响应首部字段 响应内容实体

- 零拷贝

参考nio

- netty 空轮训bug
若Selector的轮询结果为空，也没有wakeup或新消息处理，则发生空轮询，CPU使用率100%
netty 解决办法
对Selector的select操作周期进行统计，每完成一次空的select操作进行一次计数，

若在某个周期内连续发生N次空轮询，则触发了epoll死循环bug。

重建Selector，判断是否是其他线程发起的重建请求，若不是则将原SocketChannel从旧的Selector上去除注册，重新注册到新的Selector上，并将原来的Selector关闭。

# redis
（面试）
redis 协议 resp 
- 状态回复（status reply）的第一个字节是 "+"
- 错误回复（error reply）的第一个字节是 "-"
- 整数回复（integer reply）的第一个字节是 ":"
- 批量回复（bulk reply）的第一个字节是 "$"
- 多条批量回复（multi bulk reply）的第一个字节是 "*"

举例
```
SET mykey myvalue
## 协议传输实际是

*3\r\n$3\r\nSET\r\n$5\r\mykey\r\n$7\r\myvalue\r\n

```
redis五种数据类型 list String  Hash Set Zset(结构为hash 和跳表)

redis 持久化 AOF RDB

redis 缓存雪崩、缓存穿透、缓存预热、缓存更新、缓存降级等问题

redis 集群方案


redis key value值过大的问题


# 算法
（面试）
## 数据结构

- 完全二叉树特性

最后一个非叶子节点 length/2

叶子节点数 n / 2 向上取整，或(n+1) / 2 向下取整

大顶堆 左右孩子小于父节点

节点n的左子树和右子树分别是2n、2n+1

堆排序

将数组排成大顶堆

将堆顶元素和最后一个交换 重复

二叉排序树

左子节点小于父节点 右子节点大于父节点 BST

左右旋转

以手为标准 代码中以新建节点来实现 左高右低 右旋 右高左低 左旋

左子树的右子树高度大于右子树 双旋转

先对左节点左旋 在正常右旋

- 2-3 树

所有叶子节点在同一层 有两个子节点的叫二节点，三个的叫三节点 二节点要么有两个节点要么没有 一个节点可以放多个数据 （数据项）

- B树 B+ B*

B树的阶 最高节点的个数 关键字集合分布在整个树中 B+树所有数据存放叶子节点的链表中 非叶子节点为稀疏索引，叶子节点为稠密索引 B* 树加了兄弟指针

- 图

顶点——节点 边——连线 路径 无向图：顶点连线没有方向 有向图 带权图：边带权值的图

图的表示方法

邻接矩阵 邻接表 表示顶点直接相邻关系 相连的用1表示，不连用1表示 邻接表，存储每个节点的连接节点 数组加链表

图的遍历

深度优先遍历: DFS 从初始访问节点出发，首先访问第一个连接点，继续往下 广度优先遍历：BFS 分层次搜索，需要使用队列。访问所有连接点

## 常用算法
动态规划

用二维数组

kmp

for(int i =1,j=0 i<lenght,i++){ while(j>0&& str.charAt(i)!=str.charAt(j)){ j=next[j-1] } if(str.charAt(i)==str.charAt(j)){ next[i]=j } }

贪心算法

每次拿最好的结果 图路径的问题，最大权值

普利姆算法

先找图的遍历点，然后找最短路径，然后作为一个子图找子图和其他点的最短路径作为下一个子图

克鲁斯卡尔算法

每次选择权值最小的边且不构成回路（路径不唯一），直到所有的边相连 需要解决的问题，终点问题，终点不能重复（回路）。

迪杰斯特拉算法

求最短路径算法，广度优先的搜索算法

三个数组 一个记录出发顶点到各个顶点的最短距离，一个记录前驱节点，一个记录是否访问

佛洛依德算法

维护了两个二维数组 一个记录了初始顶点的前驱表，一个是顶点之间距离

将a作为中间顶点的情况 c-a-b 更新前驱表

排序

# linux

（面试）
linux 目录含义

bin 二进制文件 dev 外接设备（需要挂载） etc 配置文件 home user 目录 proc 存储linux运行进程 root root用户的目录 sbin 超级管理员权限的二进制文件 tmp 临时文件 usr:用户软件 var 日志文件夹 mnt 当外接设备需要挂载时，挂载到该目录下

linux 一切皆文件 指令的一般格式 指令主体 [选项] [操作对象，不指定一般为当前对象]

指令：

```
#  h参数一般是更加人性化的输出数据
 ls -lh /root 
#  输出如下格式 第一位 d 表示文件夹 如果是文件则是 -
 drwxr-xr-x.  2 root       root 4096 4月  11 2018 adm

# 当前路径
pwd 

mkdir -p touch # 创建文件

cp -r

df -h # 查看磁盘

free -m # 查看内存使用情况 -m表示 以mb为单位

head # 查看文件前n行 默认10 head -n xxx.txt

tail -n # 查看最后几行 
less # 较少内容查看文件
wc # 统计文件内容信息
date # 查看日期

# 查看服务器进程信息 -e 等价于 -A 显示全部进程 -f 显示字段 

pid 进程id ppid 父进程id ppid 不存在为僵尸进程 c 表示cpu 占用率 stime 启动时间 cmd 进程的名称或者对应的路径
ps -ef 

#软连接
ln
# 查看服务器进程占用资源
top

find /etc -name 'xxx'

find /etc -type f

find /etc -type d

service start stop restart

kill 66666 (pid )

ifconfig

reboot # 重启

shutdown # 关机

uptime # 开机时间

#  net状态
# -t 只显示tcp协议
# -n 信息转化
# -l 过滤 状态列的值为 listen的连接
#-p 显示发起连接的进程pid
netstat -tnlp

uname # 获取计算机系统信息

man #命令手册

#添加用户 
# -g 指定用户组
# -G 指定用户附加组
# -u 指定用户id
useradd

#修改用户 
usermod

# 设置用户密码 Linux 不允许无密码用户 无密码处于锁定状态
passwd xxx 回车
```

Linux的运行模式
linux 存在一个init 进程 pid 为1 该进程存在一个配置文件 inittab (/etc/inittab)
用户级别 0 关机 1单用户 2多用户 3完全多用户 4其他

用户信息 /etc/shadow 用户名：加密密码：最后一次修改时间：最小修改时间间隔：密码有效期：密码需要变更前的警告天数：密码过期后的宽限时间：账号失效时间：保留字段

切换用户 su xxx

删除用户 userdel -r删除对应文件夹 用户组 用户组的增删改查操作的是 /etc/group

groupadd -g(同用户 -u) groupdel

网络设置

网卡配置文件位置： /etc/sysconfig/network-scripts

网卡文件命名规则 ifcfg-xxx
# maven

### 常用命令

> 1. 创建Maven的普通java项目： 
>    mvn archetype:create 
>    -DgroupId=packageName 
>    -DartifactId=projectName  
> 2. 创建Maven的Web项目：   
>    mvn archetype:create 
>    -DgroupId=packageName    
>    -DartifactId=webappName 
>    -DarchetypeArtifactId=maven-archetype-webapp    
> 3. 编译源代码： mvn compile 
> 4. 编译测试代码：mvn test-compile    
> 5. 运行测试：mvn test   
> 6. 产生site：mvn site   
> 7. 打包：mvn package   
> 8. 在本地Repository中安装jar：mvn install 
> 9. 清除产生的项目：mvn clean   
> 10. 生成eclipse项目：mvn eclipse:eclipse  
> 11. 生成idea项目：mvn idea:idea  
> 12. 组合使用goal命令，如只打包不测试：mvn -Dtest package   
> 13. 编译测试的内容：mvn test-compile  
> 14. 只打jar包: mvn jar:jar  
> 15. 只测试而不编译，也不测试编译：mvn test -skipping compile -skipping test-compile 
>       ( -skipping 的灵活运用，当然也可以用于其他组合命令)  
> 16. 清除eclipse的一些系统设置:mvn eclipse:clean  