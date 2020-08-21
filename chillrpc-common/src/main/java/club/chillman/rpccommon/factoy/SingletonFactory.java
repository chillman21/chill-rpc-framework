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
    private static Map<String, Object> objectMap = new HashMap<>();

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> c) {
        String key = c.toString();
        Object instance = objectMap.get(key);
        if (instance != null) return c.cast(instance);
        synchronized (c) {
            if (instance == null) {
                try {
                    instance = c.newInstance();
                    objectMap.put(key, instance);
                } catch (IllegalAccessException | InstantiationException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
        return c.cast(instance);
    }
}
