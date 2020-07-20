package club.chillman.rpccore.loadbalance;

import club.chillman.rpccore.loadbalance.random.RandomLoadBalance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author NIU
 * @createTime 2020/7/21 0:22
 */
@AllArgsConstructor
@Getter
@ToString
public enum BalanceTypeEnum {
    RANDOM_ACCESS(new RandomLoadBalance());
    private LoadBalance loadBalance;

}
