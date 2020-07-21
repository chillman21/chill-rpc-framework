package club.chillman.rpccommon.utils.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 线程池工厂工具类
 * @author NIU
 * @createTime 2020/7/21 20:38
 */
@Slf4j
public final class ThreadPoolFactoryUtils {

    /**
     * 相同 threadNamePrefix 的线程池代表同一业务场景
     * TODO :通过信号量机制( {@link Semaphore} 满足条件)限制创建的线程池数量（线程池和线程不是越多越好）
     * key: threadNamePrefix
     * value: threadPool
     */
    private static Map<String, ExecutorService> threadPools = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtils() {
    }



    public static ExecutorService createDefaultThreadPoolIfAbsent(String threadNamePrefix) {
        DefaultThreadPoolConfig defaultThreadPoolConfig = new DefaultThreadPoolConfig();
        return createDefaultThreadPoolIfAbsent(defaultThreadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createDefaultThreadPoolIfAbsent(String threadNamePrefix, DefaultThreadPoolConfig defaultThreadPoolConfig) {
        return createDefaultThreadPoolIfAbsent(defaultThreadPoolConfig, threadNamePrefix, false);
    }

    /**
     * 如果指定前缀下的线程池ThreadPoolExecutor不存在，就创建一个默认配置的线程池
     * @param defaultThreadPoolConfig 默认的配置实例
     * @param threadNamePrefix 线程名前缀
     * @param daemon 线程工厂类创建的线程是否为守护线程
     * @return ThreadPoolExecutor实例
     */
    private static ExecutorService createDefaultThreadPoolIfAbsent(DefaultThreadPoolConfig defaultThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        /**
         * computeIfAbsent
         * 如果指定的键不存在，则尝试使用给定的映射函数计算其Value值，
         * 并将 Key 和 Value 存入Map中，除非{@code null}。整个方法调用是以【原子方式】执行的，
         * 因此每个键最多应用一次函数。在计算过程中，
         * 可能会阻止其他线程在该映射上尝试的某些更新操作，
         * 因此计算应简短，并且不得尝试更新此映射的任何其他映射。
         */
        ExecutorService threadPoolService = threadPools.computeIfAbsent(threadNamePrefix, k -> createThreadPool(defaultThreadPoolConfig, threadNamePrefix, daemon));
        // 如果 threadPool 被 shutdown 的话就重新创建一个
        if (threadPoolService.isShutdown() || threadPoolService.isTerminated()) {
            threadPools.remove(threadNamePrefix);
            threadPoolService = createThreadPool(defaultThreadPoolConfig, threadNamePrefix, daemon);
            threadPools.put(threadNamePrefix, threadPoolService);
        }
        return threadPoolService;
    }

    /**
     * shutDown 所有线程池
     */
    public static void shutDownAllThreadPool() {
        log.info("call shutDownAllThreadPool method");
        //parallelStream提供了流的并行处理，其底层使用Fork/Join框架实现,是多线程异步任务的一种实现
        threadPools.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                //任务成功完成或失败直到超时
                executorService.awaitTermination(7, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
                executorService.shutdownNow();
            }
        });
    }

    private static ExecutorService createThreadPool(DefaultThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(), customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 创建 ThreadFactory 。如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon           指定是否为 Daemon Thread(守护线程)
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        //非守护线程
                        .setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * 打印线程池的状态
     *
     * @param threadPool 线程池对象
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }


}
