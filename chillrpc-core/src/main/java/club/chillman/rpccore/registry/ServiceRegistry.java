package club.chillman.rpccore.registry;

import java.net.InetSocketAddress;

/**
 * @author NIU
 * @createTime 2020/7/21 20:19
 */
public interface ServiceRegistry {
    /**
     * 注册服务
     *
     * @param serviceName       服务名称
     * @param inetSocketAddress 提供服务的地址
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);
}
