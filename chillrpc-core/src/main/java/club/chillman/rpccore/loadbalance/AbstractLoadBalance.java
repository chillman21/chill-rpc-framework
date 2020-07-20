package club.chillman.rpccore.loadbalance;

import java.util.List;

/**
 * @author NIU
 * @createTime 2020/7/21 0:09
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceAddresses) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses);
    }

    /**
     * 负载均衡抽象方法
     * @param serviceAddresses 服务地址列表
     * @return string 目标服务地址
     */
    protected abstract String doSelect(List<String> serviceAddresses);
}