package club.chillman.rpccore.discovery;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 *
 * @author NIU
 * @createTime 2020/7/20 13:59
 */
public interface ServiceDiscovery {
    /**
     * 查找服务
     *
     * @param serviceName 服务名称
     * @return 提供服务的地址
     */
    InetSocketAddress findService(String serviceName);
}
