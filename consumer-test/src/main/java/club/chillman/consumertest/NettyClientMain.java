package club.chillman.consumertest;

import club.chillman.Test;
import club.chillman.TestService;

/**
 * 服务消费者测试类
 *
 * @author NIU
 * @createTime 2020/7/20 1:48
 */
public class NettyClientMain {
    public static void main(String[] args) {
        ClientTransport rpcClient = new NettyClientTransport();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient);
        TestService testService = rpcClientProxy.getProxy(TestService.class);
        String hello = testService.hello(new Test("111", "222"));
        System.out.println(hello);
    }
}
