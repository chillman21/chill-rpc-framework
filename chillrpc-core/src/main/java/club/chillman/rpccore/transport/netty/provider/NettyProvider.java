package club.chillman.rpccore.transport.netty.provider;

import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccore.config.DefaultShutdownHook;
import club.chillman.rpccore.handler.provider.NettyProviderHandler;
import club.chillman.rpccore.registry.ServiceRegistry;
import club.chillman.rpccore.registry.ZooKeeperServiceRegistry;
import club.chillman.rpccore.serialize.kryo.KryoSerializer;
import club.chillman.rpccore.supplier.ServiceSupplier;
import club.chillman.rpccore.supplier.ServiceSupplierImpl;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import club.chillman.rpccore.transport.netty.codec.kryo.NettyKryoDecoder;
import club.chillman.rpccore.transport.netty.codec.kryo.NettyKryoEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 服务提供端。接收消费者端消息，并且根据消费者端的消息调用相应的方法，然后返回结果给消费者端。
 * @author NIU
 * @createTime 2020/7/21 19:49
 */
@Slf4j
public class NettyProvider {
    private String host;
    private int port;
    private KryoSerializer kryoSerializer;
    private ServiceRegistry serviceRegistry;
    private ServiceSupplier serviceSupplier;

    public NettyProvider(String host, int port) {
        this.host = host;
        this.port = port;
        kryoSerializer = new KryoSerializer();
        serviceRegistry = new ZooKeeperServiceRegistry();
        serviceSupplier = new ServiceSupplierImpl();
    }

    public <T> void publishService(T service, Class<T> serviceClass) {
        serviceSupplier.putService(service, serviceClass);
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }
    private void start() {
        DefaultShutdownHook defaultShutdownHook = SingletonFactory.getInstance(DefaultShutdownHook.class);
        defaultShutdownHook.clearAll();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 对于boss线程组，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            ch.pipeline().addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RemoteRequest.class));
                            ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RemoteResponse.class));
                            ch.pipeline().addLast(new NettyProviderHandler());
                        }
                    })
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128);

            // 绑定端口，同步等待绑定成功,只有主线程阻塞
            ChannelFuture future = serverBootstrap.bind(host, port).sync();
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("occur exception when start server:", e);
        } finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
