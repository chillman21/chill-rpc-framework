package club.chillman.rpccore.supplier.impl;

import club.chillman.rpccommon.enumeration.RemoteErrorMessageEnum;
import club.chillman.rpccommon.exception.RemoteException;
import club.chillman.rpccore.supplier.ServiceSupplier;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现了 ServiceSupplier 接口，可以将其看做是一个保存和提供服务实例对象的实例
 * @author NIU
 * @createTime 2020/7/21 20:05
 */
@Slf4j
public class ServiceSupplierImpl implements ServiceSupplier {
    /**
     * 接口名和服务的对应关系
     * note:处理一个接口被两个实现类实现的情况如何处理？（通过 group 分组）
     * key:service/interface name
     * value:service
     */
    private static Map<String, Object> serviceMap = new ConcurrentHashMap<>();
    private static Set<String> registeredService = ConcurrentHashMap.newKeySet();

    @Override
    public <T> void putService(T service, Class<T> serviceClass) {
        //getCanonicalName返回全限定类名
        String serviceName = serviceClass.getCanonicalName();
        if (registeredService.contains(serviceName)) {
            return;
        }
        registeredService.add(serviceName);
        serviceMap.put(serviceName, service);
        log.info("添加服务:【{}】，服务接口名为:【{}】", serviceName, service.getClass().getInterfaces());
    }

    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (null == service) {
            throw new RemoteException(RemoteErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }
}
