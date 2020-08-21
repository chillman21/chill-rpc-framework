package club.chillman.rpccore.loadbalance;

import club.chillman.rpccommon.factoy.SingletonFactory;
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
    RANDOM_ACCESS(SingletonFactory.getInstance(RandomLoadBalance.class)),
    CONSISTENT_HASH(SingletonFactory.getInstance(ConsistentHashLoadBalance.class)),
    ROUND_ROBIN(SingletonFactory.getInstance(RoundRobinLoadBalance.class));

    private LoadBalance loadBalance;

}
