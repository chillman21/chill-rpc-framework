package club.chillman.rpccore.handler.consumer;

import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccore.transport.netty.consumer.UnprocessedRequests;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/** TODO
 * @author NIU
 * @createTime 2020/7/20 23:56
 */
@Slf4j
public class NettyConsumerHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;

    public NettyConsumerHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    /**
     * 读取服务端传输的消息
     * @param ctx 上下文对象
     * @param msg 消息对象
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 处理Consume端消息发生异常的时候被调用
     * @param ctx 上下文对象
     * @param cause 异常
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
