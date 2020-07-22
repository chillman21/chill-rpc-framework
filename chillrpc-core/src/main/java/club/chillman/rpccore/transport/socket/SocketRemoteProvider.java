package club.chillman.rpccore.transport.socket;

import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccommon.utils.concurrent.ThreadPoolFactoryUtils;
import club.chillman.rpccore.config.DefaultShutdownHook;
import club.chillman.rpccore.registry.ServiceRegistry;
import club.chillman.rpccore.registry.ZooKeeperServiceRegistry;
import club.chillman.rpccore.supplier.ServiceSupplier;
import club.chillman.rpccore.supplier.ServiceSupplierImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @author NIU
 * @createTime 2020/7/22 11:44
 */
@Slf4j
public class SocketRemoteProvider {

    private final ExecutorService threadPool;
    private final String host;
    private final int port;
    private final ServiceRegistry serviceRegistry;
    private final ServiceSupplier serviceSupplier;

    public SocketRemoteProvider(String host, int port) {
        this.host = host;
        this.port = port;
        threadPool = ThreadPoolFactoryUtils.createDefaultThreadPoolIfAbsent("socket-provider-pool");
        serviceRegistry = new ZooKeeperServiceRegistry();
        serviceSupplier = new ServiceSupplierImpl();
    }

    public <T> void publishService(T service, Class<T> serviceClass) {
        serviceSupplier.putService(service, serviceClass);
        serviceRegistry.registerService(serviceClass.getCanonicalName(), new InetSocketAddress(host, port));
        start();
    }

    private void start() {
        try (ServerSocket provider = new ServerSocket()) {
            provider.bind(new InetSocketAddress(host, port));
            DefaultShutdownHook shutdownHook = SingletonFactory.getInstance(DefaultShutdownHook.class);
            shutdownHook.clearAll();
            Socket socket = null;
            while ((socket = provider.accept()) != null) {
                log.info("client connected [{}]", socket.getInetAddress());
                threadPool.execute(new SocketRemoteRequestRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            log.error("occur IOException:", e);
        }
    }
}
