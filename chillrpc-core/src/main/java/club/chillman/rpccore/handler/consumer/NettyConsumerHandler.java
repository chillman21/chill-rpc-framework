package club.chillman.rpccore.handler.consumer;

import club.chillman.rpccommon.enumeration.RemoteMessageTypeEnum;
import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import club.chillman.rpccore.transport.netty.consumer.ChannelPool;
import club.chillman.rpccore.transport.netty.consumer.UnprocessedRequests;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 *
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
     * 读取服务提供端传来的消息
     * @param ctx 上下文对象
     * @param msg 消息对象
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive msg: [{}]", msg);
            RemoteResponse remoteResponse = (RemoteResponse) msg;
            unprocessedRequests.complete(remoteResponse);
        } finally {
            /**
             * 其实是ByteBuf.release()方法的包装,方便JVM回收
             * 每一个新分配的ByteBuf的引用计数值为1，
             * 每对这个ByteBuf对象增加一个引用，需要调用ByteBuf.retain()方法，
             * 而每减少一个引用，需要调用ByteBuf.release()方法，
             * 当这个ByteBuf对象的引用计数值为0时，表示此对象可回收
             */
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 写空闲事件触发心跳报文
     * @param ctx 上下文对象
     * @param evt 事件对象
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = ChannelPool.get((InetSocketAddress) ctx.channel().remoteAddress());
                //心跳报文包生成
                RemoteRequest remoteRequest = RemoteRequest.builder().remoteMessageTypeEnum(RemoteMessageTypeEnum.HEART_BEAT).build();
                //发送心跳报文，若发送时失败则关闭channel
                channel.writeAndFlush(remoteRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 处理Consume端消息发生异常的时候被调用
     * @param ctx 上下文对象
     * @param cause 异常
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("consumer端捕获到异常：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
