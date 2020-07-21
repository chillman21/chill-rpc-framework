package club.chillman.rpccore.supplier;

/**
 * @author NIU
 * @createTime 2020/7/21 19:54
 */
public interface ServiceSupplier {
    /**
     * 保存服务实例对象和服务实例对象实现的接口类的对应关系
     *
     * @param service      服务实例对象
     * @param serviceClass 服务实例对象实现的接口类
     * @param <T>          服务接口的类型
     */
    <T> void putService(T service, Class<T> serviceClass);

    /**
     * 获取服务实例对象
     *
     * @param serviceName 服务实例对象实现的接口类的类名
     * @return 服务实例对象
     */
    Object getService(String serviceName);
}
