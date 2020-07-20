package club.chillman.consumertest;

import club.chillman.Test;
import club.chillman.TestService;
import club.chillman.rpccore.transport.ConsumerTransport;
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
        RpcClientProxy rpcClientProxy = new RpcClientProxy(remoteConsumer);
        TestService testService = rpcClientProxy.getProxy(TestService.class);
        String hello = testService.hello(new Test("111", "222"));
        System.out.println(hello);
    }
}
