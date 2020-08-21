package club.chillman.rpccore.handler.reconnect;

import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccore.transport.netty.common.ChannelPool;
import club.chillman.rpccore.transport.netty.consumer.ConsumerTransport;
import club.chillman.rpccore.transport.netty.consumer.NettyConsumer;
import club.chillman.rpccore.transport.netty.consumer.NettyConsumerTransport;
import club.chillman.rpccore.transport.reconnect.RetryPolicy;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author NIU
 * @createTime 2020/8/20 23:27
 */
@Slf4j
@ChannelHandler.Sharable
public class ReconnectHandler extends ChannelInboundHandlerAdapter {
    private int retries = 0;
    private RetryPolicy retryPolicy;
    private ConsumerTransport transport;
    private InetSocketAddress inetSocketAddress;

    public ReconnectHandler(ConsumerTransport transport) {
        this.transport = transport;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Successfully established a connection to the server.");
        retries = 0;
        if (ctx.channel().isActive()) {
            inetSocketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        }
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (retries == 0) {
            log.error("Lost the TCP connection with the server.");
            ctx.close();
        }
        NettyConsumer consumer = SingletonFactory.getInstance(NettyConsumer.class);
        while (getRetryPolicy().allowRetry(retries)) {
            if (ChannelPool.isActive(inetSocketAddress)) break;
            long sleepTimeMs = getRetryPolicy().getSleepTimeMs(retries);
            log.info("Try to reconnect to the server after {}ms. Retry count: {}.", sleepTimeMs,  ++retries);
            final EventLoop eventLoop = ctx.channel().eventLoop();
            eventLoop.schedule(() -> {
                log.info("Reconnecting ...");
                //实现断线重连
                System.out.println("0000"+ inetSocketAddress);
                Channel channel = consumer.doConnect(inetSocketAddress);
                if (channel.isActive()) {
                    ChannelPool.put(inetSocketAddress, channel);
                    log.info("reconnection success!!");
                    return;
                }
            }, sleepTimeMs, TimeUnit.MILLISECONDS);
        }
        ctx.fireChannelInactive();
    }


    private RetryPolicy getRetryPolicy() {
        if (this.retryPolicy == null) {
            this.retryPolicy = ((NettyConsumerTransport) transport).getRetryPolicy();
        }
        return this.retryPolicy;
    }
}
