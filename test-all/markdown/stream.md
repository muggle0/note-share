1. 创建Stream
   一个数据源（如：集合，数组）获取一个流
2. 中间操作
   一个中间操作链，对数据源的数据进行处理
3. 终止操作（终端操作）
   一个终止操作，执行中间操作链，并产生结果

创建stream的方法

1. Collection提供了两个方法.stream()与paralleStream()

```java
List<Integer> list = new ArrayList<>();
        Stream<Integer> stream = list.stream();//串行流
        Stream<Integer> integerStream = list.parallelStream();//并行流

```

通过Arrays中的Stream()获取一个数组流。

```java
Integer[] integers ={};
 Stream<Integer> stream1 = Arrays.stream(integers);
```

通过Stream类中静态方法of()

```java
Stream<String> stream2 = Stream.of("aaa", "bbb");
```

创建无限流（无穷的数据）