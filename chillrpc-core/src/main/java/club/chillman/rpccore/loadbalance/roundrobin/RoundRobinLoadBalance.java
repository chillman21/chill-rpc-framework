package club.chillman.rpccore.loadbalance.roundrobin;

import club.chillman.rpccore.loadbalance.AbstractLoadBalance;
import club.chillman.rpccore.loadbalance.LoadBalance;
import club.chillman.rpccore.transport.dto.RemoteRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡算法 - 轮询
 * @author NIU
 * @createTime 2020/7/22 20:58
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private AtomicInteger index = new AtomicInteger(0);

    @Override
    protected String doSelect(List<String> serviceAddresses, RemoteRequest remoteRequest) {
        if(serviceAddresses.size() == 0) {
            return null;
        }
        String address = serviceAddresses.get(index.intValue());
        index.set(index.incrementAndGet() % serviceAddresses.size());
        return address;
    }
}
