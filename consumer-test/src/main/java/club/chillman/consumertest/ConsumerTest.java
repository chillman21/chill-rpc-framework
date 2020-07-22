package club.chillman.consumertest;

import club.chillman.rpccore.proxy.RemoteConsumerProxy;
import club.chillman.rpccore.transport.netty.consumer.ConsumerTransport;
import club.chillman.rpccore.transport.netty.consumer.NettyConsumerTransport;
import club.chillman.serviceapi.Test;
import club.chillman.serviceapi.TestService;

/**
 * @author NIU
 * @createTime 2020/7/22 14:39
 */
public class ConsumerTest {
    public static void main(String[] args) {
        ConsumerTransport consumerTransport = new NettyConsumerTransport();
        RemoteConsumerProxy remoteProxy = new RemoteConsumerProxy(consumerTransport);
        TestService testService = remoteProxy.getProxyObject(TestService.class);
        System.out.println(testService.wow(new Test("aaa", 1)));
    }
}
