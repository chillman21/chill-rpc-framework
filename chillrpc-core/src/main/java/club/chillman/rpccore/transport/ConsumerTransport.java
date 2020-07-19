package club.chillman.rpccore.transport;
/**
 *  消费者的RPC请求发送接口
 *
 * @author NIU
 * @createTime 2020/7/20 2:31
 */
public interface ConsumerTransport {
    /**
     *  发送
     * @param rpcRequest 消息体
     * @return 服务提供者返回的数据
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
