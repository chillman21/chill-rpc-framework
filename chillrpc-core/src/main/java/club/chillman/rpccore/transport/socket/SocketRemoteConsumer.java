package club.chillman.rpccore.transport.socket;

import club.chillman.rpccommon.exception.RemoteException;
import club.chillman.rpccore.discovery.ServiceDiscovery;
import club.chillman.rpccore.discovery.ZooKeeperServiceDiscovery;
import club.chillman.rpccore.transport.netty.consumer.ConsumerTransport;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


/**
 * 使用jdk原生 Socket 传输 RemoteRequest
 * @author NIU
 * @createTime 2020/7/21 16:34
 */
@AllArgsConstructor
@Slf4j
public class SocketRemoteConsumer implements ConsumerTransport {
    private final ServiceDiscovery serviceDiscovery;

    public SocketRemoteConsumer() {
        this.serviceDiscovery = new ZooKeeperServiceDiscovery();
    }

    @Override
    public Object sendRemoteRequest(RemoteRequest remoteRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.findService(remoteRequest.getInterfaceName(),remoteRequest);
        // try块退出时，会自动调用socket.close()方法，关闭资源。
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // 通过输出流发送数据RemoteRequest到服务端
            objectOutputStream.writeObject(remoteRequest);
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            //从输入流中读取出 RemoteResponse
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("服务[{}]调用失败",remoteRequest.getMethodName());
            throw new RemoteException("调用服务失败:", e);
        }
    }
}
