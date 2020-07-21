package club.chillman.rpccore.handler;

import club.chillman.rpccommon.enumeration.RemoteResponseCode;
import club.chillman.rpccommon.exception.RemoteException;
import club.chillman.rpccore.supplier.ServiceSupplier;
import club.chillman.rpccore.supplier.ServiceSupplierImpl;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author NIU
 * @createTime 2020/7/22 0:53
 */
@Slf4j
public class RemoteRequestHandler {
    private static ServiceSupplier serviceSupplier= new ServiceSupplierImpl();

    /**
     * 处理RPC请求，调用对应的方法，然后返回方法执行结果
     */
    public Object handle(RemoteRequest remoteRequest) {
        //通过注册中心获取到目标类（Consumer端需要调用类）
        Object service = serviceSupplier.getService(remoteRequest.getInterfaceName());
        return invokeTargetMethod(remoteRequest, service);
    }

    private Object invokeTargetMethod(RemoteRequest remoteRequest, Object service) {
        Object result = null;
        try {
            Method method = service.getClass().getMethod(remoteRequest.getMethodName(), remoteRequest.getParamTypes());
            if (null == method) {
                return RemoteResponse.error(RemoteResponseCode.METHOD_NOT_FOUND);
            }
            result = method.invoke(service, remoteRequest.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", remoteRequest.getInterfaceName(), remoteRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RemoteException(e.getMessage(), e);
        }
        return result;
    }
}
