package club.chillman.rpccore.loadbalance;

import club.chillman.rpccore.loadbalance.consistenthash.ConsistentHashLoadBalance;
import club.chillman.rpccore.loadbalance.random.RandomLoadBalance;
import club.chillman.rpccore.loadbalance.roundrobin.RoundRobinLoadBalance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 负载均衡枚举类
 * @author NIU
 * @createTime 2020/7/21 0:22
 */
@AllArgsConstructor
@Getter
@ToString
public enum BalanceTypeEnum {
    RANDOM_ACCESS(new RandomLoadBalance()),
    CONSISTENT_HASH(new ConsistentHashLoadBalance()),
    ROUND_ROBIN(new RoundRobinLoadBalance());

    private LoadBalance loadBalance;

}
