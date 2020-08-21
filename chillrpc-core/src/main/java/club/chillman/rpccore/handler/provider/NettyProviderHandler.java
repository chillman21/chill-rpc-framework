package club.chillman.rpccore.handler.provider;

import club.chillman.rpccommon.enumeration.RemoteMessageTypeEnum;
import club.chillman.rpccommon.factoy.SingletonFactory;
import club.chillman.rpccore.handler.RemoteRequestHandler;
import club.chillman.rpccore.transport.dto.RemoteRequest;
import club.chillman.rpccore.transport.dto.RemoteResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Provider端Handler
 *
 * @author NIU
 * @createTime 2020/7/22 0:20
 */
@Slf4j
public class NettyProviderHandler extends ChannelInboundHandlerAdapter {

    private final RemoteRequestHandler remoteRequestInvoker;

    public NettyProviderHandler() {
        this.remoteRequestInvoker = SingletonFactory.getInstance(RemoteRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("server receive msg: [{}] ", msg);
            RemoteRequest remoteRequest = (RemoteRequest) msg;
            if (remoteRequest.getRemoteMessageTypeEnum() == RemoteMessageTypeEnum.HEART_BEAT) {
                log.info("receive heat beat msg from client");
                return;
            }
            // 执行目标方法（客户端需要执行的方法）并且返回方法结果
            Object result = remoteRequestInvoker.handle(remoteRequest);
            log.info(String.format("server get result: %s", result.toString()));
            // Consumer端在线且通道可写
            if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                // 封装 remoteResponse 对象
                RemoteResponse<Object> remoteResponse = RemoteResponse.ok(result, remoteRequest.getRequestId());
                // 返回方法执行结果 remoteResponse 给Consumer端
                ctx.writeAndFlush(remoteResponse).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                log.error("not writable now, message dropped");
            }
        } finally {
            //确保 ByteBuf 被释放，不然可能会有内存泄露问题
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }

}
