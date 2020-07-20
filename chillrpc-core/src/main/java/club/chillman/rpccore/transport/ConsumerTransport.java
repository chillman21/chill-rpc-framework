package club.chillman.rpccore.transport;

import club.chillman.rpccore.transport.dto.RemoteRequest;

/**
 *  消费者的RPC请求发送接口
 *
 * @author NIU
 * @createTime 2020/7/20 2:31
 */
public interface ConsumerTransport {
    /**
     *  发送
     * @param remoteRequest 消息体
     * @return 服务提供者返回的数据
     */
    Object sendRemoteRequest(RemoteRequest remoteRequest);
}
