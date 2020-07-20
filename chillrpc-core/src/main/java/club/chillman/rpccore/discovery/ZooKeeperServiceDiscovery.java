package club.chillman.rpccore.discovery;

import java.net.InetSocketAddress;

/**
 * 以 Zookeeper 作为注册中心进行服务发现
 *
 * @author NIU
 * @createTime 2020/7/20 14:01
 */
public class ZooKeeperServiceDiscovery implements ServiceDiscovery{
    @Override
    public InetSocketAddress findService(String serviceName) {
        return null;
    }
}
