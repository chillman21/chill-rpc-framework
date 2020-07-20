package club.chillman.rpccore.loadbalance;

import java.util.List;

/**
 * 负载均衡顶层接口
 * @author NIU
 * @createTime 2020/7/21 0:08
 */
public interface LoadBalance {
    /**
     * 在已有服务提供地址列表中选择一个
     *
     * @param serviceAddresses 服务地址列表
     * @return 目标服务地址
     */
    String selectServiceAddress(List<String> serviceAddresses);
}
