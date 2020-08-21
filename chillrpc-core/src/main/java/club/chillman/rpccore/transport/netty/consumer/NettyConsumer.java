package club.chillman.rpccore.transport.netty.consumer;

import club.chillman.rpccommon.exception.RemoteException;
import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccore.handler.consumer.NettyConsumerHandler;
import club.chillman.rpccore.handler.reconnect.ReconnectHandler;
import club.chillman.rpccore.serialize.kryo.KryoSerializer;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import club.chillman.rpccore.transport.netty.codec.kryo.NettyKryoDecoder;
import club.chillman.rpccore.transport.netty.codec.kryo.NettyKryoEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 用于初始化和关闭 Consumer 端的 Bootstrap 对象
 * @author NIU
 * @createTime 2020/7/20 23:26
 */
@Slf4j
public final class NettyConsumer {
    private static Bootstrap bootstrap;
    // 事件循环线程组
    private static EventLoopGroup eventLoopGroup;

    static {
        eventLoopGroup = Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        KryoSerializer kryoSerializer = new KryoSerializer();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                //连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ReconnectHandler(SingletonFactory.getInstance(NettyConsumerTransport.class)));
                        //如果 15 秒之内没有发送数据给服务端的话，就发送一次心跳请求
                        /**
                         * 1. IdleStateHandler 是netty 提供的处理空闲状态的处理器
                         * 2. long readerIdleTime : 表示多长时间没有读, 就会发送一个心跳检测包检测是否连接
                         * 3. long writerIdleTime : 表示多长时间没有写, 就会发送一个心跳检测包检测是否连接
                         * 4. long allIdleTime : 表示多长时间没有读写, 就会发送一个心跳检测包检测是否连接
                         */
                        ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        /*自定义序列化编解码器*/
                        // RemoteResponse -> ByteBuf
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RemoteResponse.class));
                        // ByteBuf -> RemoteRequest
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RemoteRequest.class));
                        ch.pipeline().addLast(new NettyConsumerHandler());
                    }
                });
    }
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener(new ChannelFutureListener() {
            //这里的future就是connect事件返回的ChannelFuture对象
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("The consumer side connected to the provider successfully!");
                    completableFuture.complete(future.channel());
                } else {
                    System.out.println("****"+ future.channel());
                    future.channel().pipeline().fireChannelInactive();
                    throw new RemoteException("Consumer side failed to connect to provider!");
                }
            }
        });
        return completableFuture.get();
    }

    public void close() {
        log.info("call close method");
        eventLoopGroup.shutdownGracefully();
    }
}
