import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author NIU
 * @createTime 2020/7/21 23:52
 */
@Slf4j
public class ScheduleExecutorTest {
    public static void main(String[] args) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory());
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: []");
            log.info("===========================================");
        }, 0, 2, TimeUnit.SECONDS);
    }
}
