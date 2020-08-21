package club.chillman.rpccore.discovery;

import club.chillman.rpccore.loadbalance.BalanceTypeEnum;
import club.chillman.rpccommon.utils.ZooKeeperUtils;
import club.chillman.rpccore.loadbalance.LoadBalance;
import club.chillman.rpccore.loadbalance.random.RandomLoadBalance;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 以 Zookeeper 作为注册中心进行服务发现
 *
 * @author NIU
 * @createTime 2020/7/20 14:01
 */
@Slf4j
public class ZooKeeperServiceDiscovery implements ServiceDiscovery{
    private final LoadBalance loadBalance;

    public ZooKeeperServiceDiscovery(BalanceTypeEnum balanceTypeEnum) {
        this.loadBalance = balanceTypeEnum.getLoadBalance();
    }
    public ZooKeeperServiceDiscovery(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    public ZooKeeperServiceDiscovery() {
        this.loadBalance = new RandomLoadBalance();
    }

    @Override
    public InetSocketAddress findService(String serviceName, RemoteRequest remoteRequest) {
        // 这里直接去了第一个找到的服务地址,eg:127.0.0.1:9999
        List<String> serviceUrlList = ZooKeeperUtils.getChildrenNodes(serviceName);
        // 负载均衡
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, remoteRequest);
        log.info("Successfully found service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
