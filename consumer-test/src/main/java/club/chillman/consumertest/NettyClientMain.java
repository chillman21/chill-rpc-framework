package club.chillman.consumertest;

import club.chillman.Test;
import club.chillman.TestService;
import club.chillman.rpccore.proxy.RemoteConsumerProxy;
import club.chillman.rpccore.transport.netty.consumer.ConsumerTransport;
import club.chillman.rpccore.transport.netty.consumer.NettyConsumerTransport;

/**
 * 服务消费者测试类
 *
 * @author NIU
 * @createTime 2020/7/20 1:48
 */
public class NettyClientMain {
    public static void main(String[] args) {
        ConsumerTransport remoteConsumer = new NettyConsumerTransport();
        RemoteConsumerProxy remoteConsumerProxy = new RemoteConsumerProxy(remoteConsumer);
        TestService testService = remoteConsumerProxy.getProxyObject(TestService.class);
        String hello = testService.hello(new Test("111", "222"));
        System.out.println(hello);
    }
}
