package club.chillman.rpccore.loadbalance.roundrobin;

import club.chillman.rpccore.loadbalance.AbstractLoadBalance;
import club.chillman.rpccore.loadbalance.LoadBalance;
import club.chillman.rpccore.transport.dto.RemoteRequest;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 负载均衡算法 - 轮询
 * 如果每个服务器节点负载能力相同的话，轮询是一个简单实用的选择
 * @author NIU
 * @createTime 2020/7/22 20:58
 */
public class RoundRobinLoadBalance extends AbstractLoadBalance {

    private final LongAdder index = new LongAdder();

    @Override
    protected String doSelect(List<String> serviceAddresses, RemoteRequest remoteRequest) {
        if(serviceAddresses.size() == 0) {
            return null;
        }
        String address = serviceAddresses.get(index.intValue());
        index.increment();
        if (index.intValue() % serviceAddresses.size() == 0) {
            index.reset();
        }
        return address;
    }
}
