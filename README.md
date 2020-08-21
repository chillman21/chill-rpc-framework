# 简介

chill-rpc是一款实用的远程服务调用框架，它实现了许多高性能RPC框架的主流功能，具有包括面向接口代理的高性能RPC调用，服务注册与发现，高度可扩展等特性。这个框架我在七月底就已经基本完成，最近对它做了一些优化。

项目地址：https://github.com/chillman21/chill-rpc-framework

# 开发规约

基于复用度分包，总是⼀起使用的放在同⼀包下，将接口和基类分成独立模块，大的实现也使用独立模块。 所有接口都放在模块的根包下，基类放在 support子包下，不同实现类放在以扩展点名字命名的子包下。

# 功能列表

- 基于Netty实现长连接式的RPC，包括**心跳保持**、**断线重连**、解决**粘包半包**等。

- 基于动态代理实现透明RPC，为调用者屏蔽了底层的复杂过程。

- 基于Zookeeper实现分布式服务注册与发现，支持服务多个节点，并实现了**轮询、随机、一致性哈希**等负载均衡算法。

- 参考Dubbo部分模块实现了分层结构，如 config,proxy,loadbanlance,registry,transport,serialize等层。

  ![分层结构](http://pic.chillman.club/chillrpc1.png)

- 高性能的服务调用。配置为windows 10 pro+ i5-9300H 4核的情况下(需要说明的一点是：Windows下NioEventLoop不支持最高性能的epoll系统调用，只能用select)，使用淘宝提供的QPS压测工具，单机qps达到3000+。

  ![qps](http://pic.chillman.club/QPS%E6%B5%8B%E8%AF%951.png )

- 实现了kryo、Protostuff、json和jdk原生四种序列化方式。

- 实现了Netty传输和传统Socket传输两种传输模式。

- 定义了自己的传输协议，使用定长消息头+消息体解决粘包问题。同时使用一个RPC请求/响应的实体类包装请求ID，接口名称，方法名称，参数，是否是心跳报文。

# 项目已经做了的优化/解决的问题

- 为了循序渐进，一开始使用jdk原生BIO socket实现的(现今版本依然保留这种方式，可以自由选择)，后来使用基于NIO的netty，提高了吞吐量和效率。
- Netty 重用 Channel ，既可以提高效率，又可以避免重复连接服务端。
- 服务缓存：Consumer端第一次服务发现时将获取到的节点地址的列表缓存下来，并用一个监视器注册监听指定节点，保证节点变化时缓存与ZK的一致性，根据**局部性原理**，后续很有可能依然请求find这个服务，这时直接用缓存，减少了和ZK服务器的通讯损耗，大大提高了速度和吞吐量。
- 解决半包拆包的问题：产生粘包和拆包问题的主要原因是，操作系统在发送TCP数据的时候，底层会有一个缓冲区，例如1024个字节大小，如果一次请求发送的数据量比较小，没达到缓冲区大小，TCP则会将多个请求合并为同一个请求进行发送，这就形成了粘包问题；如果一次请求发送的数据量比较大，超过了缓冲区大小，TCP就会将其拆分为多次发送，这就是拆包，也就是将一个大的包拆分为多个小包进行发送。 我的解决方案是，在解码处理器中使用定长的整型消息长度字段(32位)作为消息头，在读取ByteBuf时就可以用readInt()方法从报文头部读取全部int类型的内容，每次核验实际消息体长度是否和消息头中一致，不一致将丢弃，一致则继续序列化。
- 很多时候我们的需求是要获取可复用的唯一的实例对象，比如Consumer对象、ChannelPool对象，这样既可以减少资源消耗又可以提高效率于是我创建了一个DCL单例工厂，只需要传入Class对象就可以获取这个对象的单例，只有在第一次获取时他才会创建对象并把这个对象存进map中，并且这个操作需要加锁。后续获取只需要从map中取出即可，取出不需要加锁。
- 断线重连的设计做了很久，在入站的上游handler中加入一个重连处理器，放在最上游是为了第一时间监听到断线事件，一旦监听到连接断开，通过一个循环，每次循环条件是当前重试次数是否到达最大次数，在循环体内重试次数自增，然后根据当前循环次数对应的策略设置定时调度任务的延迟时间，这个调度任务通过断线事件发生所在的那个线程对应的eventloop的schedule方法实现(因为每个eventloop都表示一个不断循环的执行处理任务的线程)，然后在任务体内用单例工厂获取Consumer实例，调用他的连接方法，他将返回一个Channel通道，一旦这个channel通道处于活跃状态，说明我们重连成功，然后把他放到ChannelPool中以便后续复用。
- 心跳报文的设计也是难点之一，我首先使用一个空闲状态处理器IdleStateHandler分别放在provider端和consumer端的pipeline入站的上游，为provider端设置读空闲时间，为consumer端设置写空闲时间为5s，这样consumer端一旦出现写空闲的状态，就会把空闲事件传递给下游处理器，下游处理器捕获到事件则向provider端发送心跳报文，它附带一个特殊的标志位。服务提供端会监听每一个入站的报文，一旦是心跳报文就不需要调用服务直接跳过后面的操作，这样连接就会保持；如果服务提供端读空闲时间长达30s，就会自动断开连接。
- 在负载均衡的一致性哈希算法中，增加了每个服务地址对应160个虚拟节点，把160个节点分成40组，每组都对这个服务地址名后面增加一个“- [组号]”的后缀，这样得到分组后的地址名用MD5计算出哈希值。由于MD5是128位的，我把它平均截取成四段，再把这四段分配给四个虚拟节点。虚拟节点所在的哈希环其实就是TreeMap结构，它的Key为哈希值，value为服务地址，而TreeMap提供一个tailMap方法可以获得所有Key大于指定值的子集合，那么这个集合的第一个节点就是这个请求应该负载到的哈希槽，也就是服务节点。这使得在大规模负载下有着很好的分流效果。
- 解决了Kryo非线程安全的问题：由于Kryo 不是线程安全的。每个线程都应该有自己的 Kryo Input 和 Output 实例，因此使用 ThreadLocal 存放 Kryo 对象。同时应当注意使用完threadLocal应当及时remove，否则有可能出现内存泄漏。
- 在负载均衡的轮询算法中，原本使用AtomicInteger作为轮询的索引值，每次访问则自增；这其中存在一个问题：`AtomicInteger`底层使用Unsafe类提供的CAS方法，他的底层又是由CPU指令`cmpxchg`实现的，高并发下，只能有一个线程CAS自增成功，大量线程的CAS操作将失败，这些线程将不断地自旋尝试CAS操作直到对应值设置成功，随着并发量增大，**性能会越来越差**。因此选择jdk8中新出现的LongAdder，它采用了**分治**的思想 ，当并发量不太高时其直接通过不断地在其内部成员变量base上进行自旋CAS操作设置值，这时实际与AtomicLong实现一致；当并发量很高时，不同线程的自旋CAS操作会映射到LongAdder内部Cell数组中的某一个Cell上，从而减少了单个值上面自旋CAS操作失败的次数，降低了CPU因为CAS自旋操作失败的消耗。 
- **处理一个接口有多个实现类的情况** ：对服务分组，provider端发布服务的时候增加一个 group 参数即可，这个参数会追加到ZK节点名称后缀后，consumer端调用时也需要提供这个参数(重载方法，可选)，然后会把这个参数放到request对象中，ZK客户端查找服务的时候取出这个参数进行查找。
- 在BIO Socket连接方式中，Provider端由于需要线程和连接一一对应，为了最大化地利用线程资源，采用线程池分配连接。
- 如果当前操作系统支持Epoll将自动使用，提高了吞吐量和响应速度。

# 待优化/可拓展点

- 看了一个参加阿里天池中间件大赛的top3的文章，发现可以**复用eventLoop**，如果入站的 io 线程和 出站的 io 线程使用相同的线程，可以减少不必要的上下文切换。这一点在低并发度下可能还不明显，只有 200 多 qps 的差距，但在高并发下尤为明显。 
- 集成 Spring 通过注解注册服务
- **增加可配置比如序列化方式、注册中心的实现方式,避免硬编码** ：通过 API 配置，后续集成 Spring 的话建议使用配置文件的方式进行配置

# 项目分层架构

![chillrpcprojectstructure](http://pic.chillman.club/chillrpcprojectstructure.png)

## 各层介绍

扩展点：用户可以为某个接口添加自己的实现，在不改变框架源码的前提下，对部分实现进行定制。

项目分为两个模块：chillrpc-core和chillrpc-common。

### chillrpc-common

维护了项目所需的常量、枚举类、异常处理、单例工厂以及各种工具类。其中，ZookeeperUtils提供了最底层的服务的注册、发现、缓存管理等方法。

### chillrpc-core

#### service

服务层，这一层由调用者实现，用户需要声明自己需要调用的RPC服务接口就可以调用远程服务，整个过程对用户是透明的，因此可以直接与用户的业务逻辑对接。注：如果多个实现类还需要声明服务组。

#### proxy

代理层，主要是为调用者生成接口的代理实例，使用代理类返回的对象对应的方法会隐性调用invoke()方法，这个方法会调用transport层来发送RPC请求报文。

对应的核心类：

- RemoteConsumerProxy

#### registry/discovery

注册中心层，主要是服务注册与服务发现，比如对于provider而言，在服务暴露的时候将自己的地址写入到注册中心；对于consumer而言，在服务发现的时候获取服务器的地址，并建立连接，同时维护一个服务缓存，后续调用直接走缓存。

对应的核心类：

- ServiceRegistry（扩展点，目前有Zookeeper一种实现）
- ServiceDiscovery（扩展点，目前有Zookeeper一种实现）

#### LoadBalance

负载均衡层，主要是将一个接口的多节点实现对外暴露为单个节点，屏蔽多个节点的细节，同时可以把流量平均分担给各个服务子节点。

对应的核心类：

- LoadBalance（扩展点，必须继承自AbstractLoadBalance，目前有随机、轮询、一致性哈希三种实现）

#### transport

通信层，也是最核心的一层，它包括了定义两端传输的消息体结构，TCP长连接心跳保持、断线重连、Consumer端与Provider端服务器的搭建以及通信监听管理的handler、ChannelPool等重要实现；dubbo源码中还有一层协议层，在我的项目中由于只考虑TCP一种协议，于是选择将这两层合并。

对应的核心类：

- NettyProvider

- NettyConsumer

  它们不是扩展点，只是Protocol在执行相应操作时依赖的组件。

- ConsumerTransport （扩展点，目前有NIO Netty通信和BIO 原生socket通信两种实现）

#### serialize

序列化器，发送端使用它对对象进行序列化为字节数组，接收端使用它对字节数组进行反序列化为对象，这层也是十分重要的，序列化的优劣决定了占用内存大小、传输速度、吞吐量，同时他一定要是安全无漏洞的。

对应的核心类：

- Serialzer（扩展点，目前有Kryo、Jdk、Protostuff和json四种实现）

# 一次RPC调用的流程图

![服务注册与调用](http://pic.chillman.club/%E6%9C%8D%E5%8A%A1%E6%B3%A8%E5%86%8C%E4%B8%8E%E8%B0%83%E7%94%A8.png )

# 演示

#### 服务发布

首先我们需要写一个实体类作为方法的参数，这里我用了lombok：

```java
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class Test implements Serializable {
    private static final long serialVersionUID = -3325838916375597109L;
    private String str;
    private int num;
}
```

编写服务接口：

```java
public interface TestService {
    String wow(Test test);
}
```

在Provider端编写它的实现类，这个wow()方法会获取传入参数的两项属性并返回拼接好的字符串：

```java
public class TestServiceImpl implements TestService {
    @Override
    public String wow(Test test) {
        return test.getStr() + " wow" + test.getNum();
    }
}
```

这样我们就可以将这个服务发布了：

```java
public static void main(String[] args) {
        TestService testService = new TestServiceImpl();
        NettyProvider nettyProvider = new NettyProvider("127.0.0.1", 5428);//指定服务器地址和端口
        nettyProvider.publishService(testService, TestService.class,"group1");
        nettyProvider.start();
    }
```

效果如下图，这说明发布成功：

![](http://pic.chillman.club/%E6%9C%8D%E5%8A%A1%E5%8F%91%E5%B8%83%E6%BC%94%E7%A4%BA1.png)

#### 服务调用

首先，Consumer端在调用服务前，需要将`TestService`接口以及参数实体类声明，代码和之前是一样的。下面直接开始服务调用：

```java
public static void main(String[] args) {
        ConsumerTransport consumerTransport = new NettyConsumerTransport(BalanceTypeEnum.CONSISTENT_HASH);//通信方式选择Netty，负载均衡方式选择一致性哈希
        RemoteConsumerProxy remoteProxy = new RemoteConsumerProxy(consumerTransport, "group1");//如果有多个实现类，需要指定分组
        TestService testService = remoteProxy.getProxyObject(TestService.class);
        for (int i = 0; i < 10; i++) {//连续调用十次
            System.out.println(testService.wow(new Test("aaa", i))); //可以看到，这里实现了透明的RPC调用，每次传入一个不同的Test对象
        }
    }
```

服务调用效果如下：

这是consumer端：

![](http://pic.chillman.club/consumer%E6%94%B6%E5%88%B0%E5%93%8D%E5%BA%94.png)

provider端：

![](http://pic.chillman.club/provider%E6%94%B6%E5%88%B0%E8%AF%B7%E6%B1%82.png)

#### 断线重连

![](http://pic.chillman.club/%E6%96%AD%E7%BA%BF%E9%87%8D%E8%BF%9E.png )

#### 心跳报文

consumer端写空闲状态：

![](http://pic.chillman.club/%E7%A9%BA%E9%97%B2%E7%8A%B6%E6%80%81consumer.png)

provider端成功收到心跳报文：

![](http://pic.chillman.club/%E5%BF%83%E8%B7%B3%E6%8A%A5%E6%96%87provider.png)