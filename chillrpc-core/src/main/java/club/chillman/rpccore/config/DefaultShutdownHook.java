package club.chillman.rpccore.config;

import club.chillman.rpccommon.utils.ZooKeeperUtils;
import club.chillman.rpccommon.utils.concurrent.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * JVM关闭钩子，与关闭进程并发执行
 * 用于在退出时清理已注册的所有节点，并关闭线程池
 * @author NIU
 * @createTime 2020/7/21 20:31
 */
@Slf4j
public final class DefaultShutdownHook {

//    private static final DefaultShutdownHook DEFAULT_SHUTDOWN_HOOK = new DefaultShutdownHook();
//
//    public static DefaultShutdownHook getDefaultShutdownHook() {
//        return DEFAULT_SHUTDOWN_HOOK;
//    }

    public void clearAll() {
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ZooKeeperUtils.clearRegistries();
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }
}
