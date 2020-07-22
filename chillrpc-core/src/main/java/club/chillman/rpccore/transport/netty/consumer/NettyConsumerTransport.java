package club.chillman.rpccore.transport.netty.consumer;

import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccore.discovery.ServiceDiscovery;
import club.chillman.rpccore.discovery.ZooKeeperServiceDiscovery;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import club.chillman.rpccore.transport.netty.common.ChannelPool;
import club.chillman.rpccore.transport.netty.common.UnprocessedRequests;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author NIU
 * @createTime 2020/7/20 2:28
 */
@Slf4j
public class NettyConsumerTransport implements ConsumerTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;

    public NettyConsumerTransport() {
        this.serviceDiscovery = new ZooKeeperServiceDiscovery();
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @Override
    public CompletableFuture<RemoteResponse> sendRemoteRequest(RemoteRequest remoteRequest) {
        // 构建返回值
        CompletableFuture<RemoteResponse> resultFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.findService(remoteRequest.getInterfaceName());
        Channel channel = ChannelPool.get(inetSocketAddress);
        if (channel != null && channel.isActive()) {
            // 放入未处理的请求
            unprocessedRequests.put(remoteRequest.getRequestId(), resultFuture);
            channel.writeAndFlush(remoteRequest).addListener((ChannelFutureListener) future -> {
                // 请求是否发送成功
                if (future.isSuccess()) {
                    log.info("consumer send message: [{}]", remoteRequest);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        } else {
            throw new IllegalStateException("sendRemoteRequest failed");
        }

        return resultFuture;
    }
}
