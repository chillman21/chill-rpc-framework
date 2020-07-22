package club.chillman.rpccore.transport.socket;

import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccore.handler.RemoteRequestHandler;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author NIU
 * @createTime 2020/7/22 11:54
 */
@Slf4j
public class SocketRemoteRequestRunnable implements Runnable {

    private Socket socket;
    private RemoteRequestHandler remoteRequestHandler;


    public SocketRemoteRequestRunnable(Socket socket) {
        this.socket = socket;
        this.remoteRequestHandler = SingletonFactory.getInstance(RemoteRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("server handle message from client by thread: [{}]", Thread.currentThread().getName());
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RemoteRequest remoteRequest = (RemoteRequest) objectInputStream.readObject();
            Object result = remoteRequestHandler.handle(remoteRequest);
            objectOutputStream.writeObject(RemoteResponse.ok(result, remoteRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            log.error("occur exception:", e);
        }
    }
}
