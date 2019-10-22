package xyz.asurily.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyIPHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final static Logger logger = LoggerFactory.getLogger(ProxyIPHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ChannelInfo ci = new ChannelInfo();
        ci.init(ctx.channel());
        super.channelActive(ctx);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        if (msg instanceof HttpRequest) {
            setRealRequestIp((HttpRequest) msg, ctx);
        }
        ctx.fireChannelRead(msg.retain());
    }

    private void setRealRequestIp(HttpRequest request, ChannelHandlerContext ctx) {
        try {
            ChannelInfo ci = ChannelInfo.getChannelInfo(ctx.channel());
            if (ci != null) {
                String clientIP = request.headers().get("X-Real-IP");
                logger.info("ip:{}", clientIP);
                ci.setClientIp(clientIP);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
