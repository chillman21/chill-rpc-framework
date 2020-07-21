package club.chillman.rpccore.transport.netty.provider;

import club.chillman.rpccore.serialize.kryo.KryoSerializer;
import club.chillman.rpccore.supplier.ServiceSupplier;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.spi.ServiceRegistry;

/**
 * 服务提供端。接收消费者端消息，并且根据消费者端的消息调用相应的方法，然后返回结果给消费者端。
 * @author NIU
 * @createTime 2020/7/21 19:49
 */
@Slf4j
public class NettyProvider {
    private String host;
    private int port;
    private KryoSerializer kryoSerializer;
    private ServiceRegistry serviceRegistry;
    private ServiceSupplier serviceSupplier;
}
