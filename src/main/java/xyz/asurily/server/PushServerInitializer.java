package xyz.asurily.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.asurily.bean.protocol.DexProto;

import java.util.List;

public class PushServerInitializer extends ChannelInitializer<Channel> {

    private final static Logger LOGGER = LoggerFactory.getLogger(PushServerInitializer.class);

    // 最大协议包长度
    public static final int MAX_FRAME_LENGTH = 1024 * 100; // 10k
    //
    public static final int MAX_AGGREGATED_CONTENT_LENGTH = 65536;

    private final PushServerHandle wsServerHandle = new PushServerHandle();

    private String wsUrl;

    public PushServerInitializer(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        //打印日志,可以看到websocket帧数据
        pipeline.addFirst("Log", new LoggingHandler(LogLevel.INFO));
        // 长时间不写会断
        pipeline.addLast("ReadTimeout", new ReadTimeoutHandler(120));
        // HTTP请求的解码和编码
        pipeline.addLast("HttpServerCodec", new HttpServerCodec());
        // 主要用于处理大数据流，比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的; 增加之后就不用考虑这个问题了
        pipeline.addLast("ChunkedWriter", new ChunkedWriteHandler());
        // 把多个消息转换为一个单一的FullHttpRequest或是FullHttpResponse，
        // 原因是HTTP解码器会在每个HTTP消息中生成多个消息对象HttpRequest/HttpResponse,HttpContent,LastHttpContent
        pipeline.addLast("HttpAggregator", new HttpObjectAggregator(MAX_AGGREGATED_CONTENT_LENGTH));
        // 代理IP获取
        pipeline.addLast("ProxyIP", new ProxyIPHandler());
        // WS 数据压缩
        pipeline.addLast("WsCompression", new WebSocketServerCompressionHandler());
        // 协议包长度限制
        pipeline.addLast("WsProtocolDecoder", new WebSocketServerProtocolHandler(wsUrl, null, true));
        // WS 解析出 ByteBuf
        pipeline.addLast("WsBinaryDecoder", new MessageToMessageDecoder<WebSocketFrame>() {
            @Override
            protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
                //注意给下一个解码器传递的时候，ByteBuf对象需要retain，不然ByteBuf对象会被这个解码器销毁掉
                out.add(frame.content().retain());
            }
        });
        // ByteBuf 编码成 WebSocket
        pipeline.addLast("WsEncoder", new MessageToMessageEncoder<ByteBuf>() {
            @Override
            protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
                //注意给下一个解码器传递的时候，ByteBuf对象需要retain，不然ByteBuf对象会被这个解码器销毁掉
                WebSocketFrame webSocketFrame = new BinaryWebSocketFrame(msg.retain());
                out.add(webSocketFrame);
            }
        });
        // proto 编码成 ByteBuf
        ch.pipeline().addLast("ProtoBufEncoder", new ProtobufEncoder());
        // ByteBuf 实例为 CommonProtocol类型
        pipeline.addLast("ProtoBufDecoder", new ProtobufDecoder(DexProto.DexProtocol.getDefaultInstance()));
        // 业务处理器
        pipeline.addLast(wsServerHandle);
    }
}
