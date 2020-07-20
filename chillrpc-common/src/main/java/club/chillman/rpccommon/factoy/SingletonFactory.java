package club.chillman.rpccommon.factoy;

import java.util.HashMap;
import java.util.Map;

/**
 * 单例对象工厂模式
 *
 * @author NIU
 * @createTime 2020/7/20 23:04
 */
public final class SingletonFactory {

    private volatile static Object INSTANCE;

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> c) {
        if (INSTANCE == null) {
            synchronized (SingletonFactory.class) {
                if (INSTANCE == null) {
                    try {
                        INSTANCE = c.newInstance();
                    } catch (IllegalAccessException | InstantiationException e) {
                        throw new RuntimeException(e.getMessage(), e.getCause());
                    }
                }
            }
        }

        return c.cast(INSTANCE);
    }
}
