package club.chillman.rpccore.proxy;

import club.chillman.rpccore.transport.ConsumerTransport;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * RPC调用过程的动态代理类。当动态代理对象调用一个方法的时候，实际调用的是下面的 invoke 方法。
 * 正是因为动态代理才让客户端调用的远程方法像是调用本地方法一样（屏蔽了中间过程）
 *
 * @author NIU
 * @createTime 2020/7/20 2:51
 */
@Slf4j
public class RemoteProxy implements InvocationHandler {

    private final ConsumerTransport consumerTransport;

    public RemoteProxy(ConsumerTransport consumerTransport) {
        this.consumerTransport = consumerTransport;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxyObject(Class<T> classType) {
        return (T) Proxy.newProxyInstance(classType.getClassLoader(), new Class<?>[]{classType}, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
