package club.chillman.rpccore.proxy;

import club.chillman.rpccore.handler.checker.RemoteMessageChecker;
import club.chillman.rpccore.transport.netty.consumer.ConsumerTransport;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import club.chillman.rpccore.transport.netty.consumer.NettyConsumerTransport;
import club.chillman.rpccore.transport.socket.SocketRemoteConsumer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * RPC调用过程的动态代理类。当动态代理对象调用一个方法的时候，实际调用的是下面的 invoke 方法。
 * 正是因为动态代理才让客户端调用的远程方法像是调用本地方法一样（屏蔽了中间过程）
 *
 * @author NIU
 * @createTime 2020/7/20 2:51
 */
@Slf4j
public class RemoteConsumerProxy implements InvocationHandler {

    private final ConsumerTransport consumerTransport;

    public RemoteConsumerProxy(ConsumerTransport consumerTransport) {
        this.consumerTransport = consumerTransport;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxyObject(Class<T> classType) {
        return (T) Proxy.newProxyInstance(classType.getClassLoader(), new Class<?>[]{classType}, this);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoked method: [{}]", method.getName());
        RemoteRequest remoteRequest = RemoteRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .requestId(UUID.randomUUID().toString())
                .build();
        RemoteResponse remoteResponse = null;
        if (consumerTransport instanceof NettyConsumerTransport) {
            CompletableFuture<RemoteResponse> completableFuture =
                    (CompletableFuture<RemoteResponse>)
                            consumerTransport.sendRemoteRequest(remoteRequest);
            remoteResponse = completableFuture.get();
        }
        else if (consumerTransport instanceof SocketRemoteConsumer) {
            remoteResponse = (RemoteResponse) consumerTransport.sendRemoteRequest(remoteRequest);
        }
        //校验 RpcResponse 和 RpcRequest
        RemoteMessageChecker.check(remoteRequest, remoteResponse);
        return remoteResponse.getData();
    }
}
