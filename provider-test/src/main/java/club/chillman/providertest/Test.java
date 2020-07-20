package club.chillman.providertest;

import club.chillman.rpccommon.utils.ZooKeeperUtils;
import org.apache.curator.framework.CuratorFramework;

/**
 * @author NIU
 * @createTime 2020/7/20 15:21
 */
public class Test {
    public static void main(String[] args) {
        CuratorFramework zkClient = ZooKeeperUtils.getZkClient();
    }
}
