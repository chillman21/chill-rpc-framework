package club.chillman.rpccore.loadbalance.random;

import club.chillman.rpccore.loadbalance.AbstractLoadBalance;
import club.chillman.rpccore.loadbalance.LoadBalance;
import club.chillman.rpccore.transport.dto.RemoteRequest;

import java.util.List;
import java.util.Random;

/**
 * @author NIU
 * @createTime 2020/7/21 0:16
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceAddresses, RemoteRequest remoteRequest) {
        Random random = new Random();
        //区间为[0,size)
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
