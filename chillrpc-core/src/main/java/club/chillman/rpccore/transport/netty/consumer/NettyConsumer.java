package club.chillman.rpccore.transport.netty.consumer;

import club.chillman.rpccore.handler.consumer.NettyConsumerHandler;
import club.chillman.rpccore.serialize.kryo.KryoSerializer;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import club.chillman.rpccore.transport.netty.codec.kryo.NettyKryoDecoder;
import club.chillman.rpccore.transport.netty.codec.kryo.NettyKryoEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * 用于初始化 和 关闭 Bootstrap 对象
 * @author NIU
 * @createTime 2020/7/20 23:26
 */
public final class NettyConsumer {
    private static Bootstrap bootstrap;
    // 事件循环线程组
    private static EventLoopGroup eventLoopGroup;

    static {
        eventLoopGroup = new NioEventLoopGroup();
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
                        //如果 15 秒之内没有发送数据给服务端的话，就发送一次心跳请求
                        ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        /*自定义序列化编解码器*/
                        // RpcResponse -> ByteBuf
                        ch.pipeline().addLast(new NettyKryoDecoder(kryoSerializer, RemoteResponse.class));
                        // ByteBuf -> RpcRequest
                        ch.pipeline().addLast(new NettyKryoEncoder(kryoSerializer, RemoteRequest.class));
                        ch.pipeline().addLast(new NettyConsumerHandler());
                    }
                });
    }

    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        return null;
    }
}
