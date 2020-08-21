package club.chillman.rpccommon.utils;

import club.chillman.rpccommon.constant.ZkConstant;
import club.chillman.rpccommon.exception.RemoteException;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.*;

/**
 * ZooKeeper工具类
 * @author NIU
 * @createTime 2020/7/20 14:23
 */
@Slf4j
public final class ZooKeeperUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 5;
    private static final String CONNECT_STRING;
    public static final String ZK_REGISTER_ROOT_PATH;
    private static Map<String, List<String>> serviceAddressMap = new ConcurrentHashMap<>();
    private static Set<String> registeredPathSet = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;

    static {//加载ZK配置
        try {
            Properties properties = new Properties();
            // 使用ClassLoader加载properties配置文件生成对应的输入流
            InputStream in = ZooKeeperUtils.class.getClassLoader().getResourceAsStream(ZkConstant.CONFIG_FILE_NAME);
            if (null != in) {
                // 使用properties对象加载输入流
                properties.load(in);
                String connectString = properties.getProperty(ZkConstant.REGISTRY_KEY);
                String rootPathKey = properties.getProperty(ZkConstant.ROOT_PATH_KEY);
                if (StringUtils.isEmpty(connectString, rootPathKey)) {
                    throw new IllegalArgumentException("配置文件错误");
                }
                CONNECT_STRING  = connectString;
                ZK_REGISTER_ROOT_PATH = rootPathKey;

            } else {
                CONNECT_STRING = ZkConstant.DEFAULT_REGISTRY_VALUE;
                ZK_REGISTER_ROOT_PATH = ZkConstant.DEFAULT_ROOT_PATH_VALUE;
            }
            zkClient = getZooKeeperClient();
        } catch (IOException e) {
            throw new RemoteException(e.getMessage(), e.getCause());
        }
    }

    private ZooKeeperUtils() {
    }

    private static CuratorFramework getZooKeeperClient() {
        // 重试策略:最大重试5次，并且会指数增加重试之间的睡眠时间。
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        CuratorFramework curatorFramework = CuratorFrameworkFactory
                .builder()
                //要连接的服务器(可以是服务器列表)
                .connectString(CONNECT_STRING)
                .retryPolicy(retryPolicy)
                .build();
        curatorFramework.start();

        return curatorFramework;
    }

    /**
     * 获取某个服务对象接口名下的子节点,也就是获取所有提供服务的生产者的地址
     *
     * @param serviceName 服务对象接口名 eg:club.chillman.TestService
     * @return 指定服务名称下的所有子节点
     */
    public static List<String> getChildrenNodes(String serviceName) {
        // 如果缓存中存在
        if (serviceAddressMap.containsKey(serviceName)) {
            return serviceAddressMap.get(serviceName);
        }
        List<String> result;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        try {
            // forPath底层源码：getChildren,并查找ServicePath的任务加入异步任务队列
            result = zkClient.getChildren().forPath(servicePath);
            serviceAddressMap.put(serviceName, result);
            registerWatcher(zkClient, serviceName);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e.getCause());
        }
        return result;
    }

    /**
     * 注册监听指定节点，保证节点变化时缓存与ZK的一致性。
     *
     * @param serviceName 服务对象接口名
     */
    private static void registerWatcher(CuratorFramework zkClient, String serviceName) {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + serviceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
             List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
             serviceAddressMap.put(serviceName, serviceAddresses);
             if (pathChildrenCacheEvent.getType() == CHILD_REMOVED) {
                serviceAddressMap.remove(serviceName);
            }
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        try {
            pathChildrenCache.start();
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 创建持久化节点。不同于临时节点，持久化节点不会因为客户端断开连接而被删除
     *
     * @param path 节点路径
     */
    public static void createPersistentNode(String path) {
        try {
            if (registeredPathSet.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("Node already exists, node is:[{}]", path);
            } else {
                //eg: /chill-rpc/club.chillman.TestService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node is created successfully. The node is:[{}]", path);
            }
            registeredPathSet.add(path);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 清空注册中心的数据
     */
    public static void clearRegistries() {
        registeredPathSet.stream().parallel().forEach(p -> {
            try {
                zkClient.delete().forPath(p);
            } catch (Exception e) {
                throw new RemoteException(e.getMessage(), e.getCause());
            }
        });
        log.info("Provider all registered services are cleared:[{}]", registeredPathSet.toString());
    }
}
