package club.chillman.providertest;

import club.chillman.rpccommon.utils.ZooKeeperUtils;
import club.chillman.rpccore.transport.netty.provider.NettyProvider;
import club.chillman.serviceapi.Test;
import club.chillman.serviceapi.TestService;
import org.apache.curator.framework.CuratorFramework;

/**
 * @author NIU
 * @createTime 2020/7/20 15:21
 */
public class ProviderTest {
    public static void main(String[] args) {
        TestService testService = new TestServiceImpl();
        NettyProvider nettyProvider = new NettyProvider("127.0.0.1", 5428);
        nettyProvider.publishService(testService, TestService.class,"group1");
        nettyProvider.start();
        //System.out.println(testService.wow(new Test("aaa", 1)));

    }
}
