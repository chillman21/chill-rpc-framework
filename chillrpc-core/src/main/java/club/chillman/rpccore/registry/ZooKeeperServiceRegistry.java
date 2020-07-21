package club.chillman.rpccore.registry;

import club.chillman.rpccommon.utils.ZooKeeperUtils;

import java.net.InetSocketAddress;

/**
 * @author NIU
 * @createTime 2020/7/21 20:20
 */
public class ZooKeeperServiceRegistry implements ServiceRegistry {
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {
        //根节点下注册子节点：服务
        String servicePath = ZooKeeperUtils.ZK_REGISTER_ROOT_PATH + "/" + serviceName + inetSocketAddress.toString();
        ZooKeeperUtils.createPersistentNode(servicePath);
    }
}
