package xyz.asurily.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.asurily.bean.protocol.DexProto;
import xyz.asurily.server.valid.DexProtocolValidator;
import xyz.asurily.server.valid.ErrorCode;
import xyz.asurily.server.valid.ValidResult;

@ChannelHandler.Sharable
public class PushServerHandle extends SimpleChannelInboundHandler<DexProto.DexProtocol> {

    private final static Logger LOGGER = LoggerFactory.getLogger(PushServerHandle.class);


    /**
     * 取消绑定
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("offline:{}", ctx.channel());
        SessionSocketHolder.offline((NioSocketChannel) ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DexProto.DexProtocol protocol) throws Exception {

        ValidResult validResult = DexProtocolValidator.validEventKey(protocol);

        ChannelInfo channelInfo = ChannelInfo.getChannelInfo(ctx.channel());
        if (channelInfo != null) {
            LOGGER.info("clientIp:{}", channelInfo.getClientIp());
            validResult.setClientIp(channelInfo.getClientIp());
        }

        //信息校验错误
        if (validResult.getErrorCode().getCode() > 0) {
            ctx.channel().writeAndFlush(
                    DexProto.DexProtocol.newBuilder()
                            .setTopics(protocol.getTopics())
                            .setOpType(protocol.getOpType())
                            .setErrorCode(validResult.getErrorCode().getCode())
                            .build());
        }

        //操作类型校验
        if (DexProtocolValidator.OpType.ping == validResult.getOpType()) {
            SessionSocketHolder.online(
                    (NioSocketChannel) ctx.channel(),
                    validResult);
            ctx.channel().writeAndFlush(
                    DexProto.DexProtocol.newBuilder()
                            .setTopics(protocol.getTopics())
                            .setOpType(DexProtocolValidator.OpType.pong.name())
                            .setErrorCode(ErrorCode.normal.getCode())
                            .build());
        }

        //订阅
        if (DexProtocolValidator.OpType.sub == validResult.getOpType()) {
            SessionSocketHolder.online(
                    (NioSocketChannel) ctx.channel(),
                    validResult);
            ctx.channel().writeAndFlush(
                    DexProto.DexProtocol.newBuilder()
                            .setTopics(protocol.getTopics())
                            .setOpType(DexProtocolValidator.OpType.sub.name())
                            .setErrorCode(ErrorCode.normal.getCode())
                            .build());
        }

        //取消订阅
        if (DexProtocolValidator.OpType.un_sub == validResult.getOpType()) {
            SessionSocketHolder.online(
                    (NioSocketChannel) ctx.channel(),
                    validResult);
            ctx.channel().writeAndFlush(
                    DexProto.DexProtocol.newBuilder()
                            .setTopics(protocol.getTopics())
                            .setOpType(DexProtocolValidator.OpType.un_sub.name())
                            .setErrorCode(ErrorCode.normal.getCode())
                            .build());
        }
    }

    //每个channel都有一个唯一的id值
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //打印出channel唯一值，asLongText方法是channel的id的全名
        LOGGER.info("channelId：{}", ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("", cause);
        //if (!DexException.isResetByPeer(cause.getMessage())) {
        //    LOGGER.error(cause.getMessage(), cause);
        //}
    }

}
