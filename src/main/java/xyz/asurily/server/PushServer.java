package xyz.asurily.server;

import com.google.protobuf.ByteString;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import xyz.asurily.bean.pojo.ConnectionUserInfo;
import xyz.asurily.bean.protocol.DexProto;
import xyz.asurily.server.valid.DexProtocolValidator;
import xyz.asurily.server.valid.ErrorCode;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;


@Component
public class PushServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(PushServer.class);

    private static final long ONW_MINUTE = 60 * 1000L;

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup work = new NioEventLoopGroup();


    @Value("${ws.server.port}")
    private int nettyPort;

    @Value("${ws.url}")
    private String wsUrl;

    /**
     * 启动 ws server
     *
     * @return
     * @throws InterruptedException
     */
    @PostConstruct
    public void start() throws InterruptedException {

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(nettyPort))
                //保持长连接
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new PushServerInitializer(wsUrl));

        ChannelFuture future = bootstrap.bind().sync();
        if (future.isSuccess()) {
            LOGGER.info("启动 dex server 成功");
        }
    }


    /**
     * 销毁
     */
    @PreDestroy
    public void destroy() {
        boss.shutdownGracefully().syncUninterruptibly();
        work.shutdownGracefully().syncUninterruptibly();
        LOGGER.info("关闭 dex server 成功");
    }


    /**
     * 发送 Google Protocol 编码消息
     */
    @Async("taskExecutor")
    public void sendMsg(String clientId, String eventKey, ByteString message) {

        LOGGER.info("thread:{}", Thread.currentThread());
        NioSocketChannel socketChannel = SessionSocketHolder.getSocket(clientId);
        if (null == socketChannel) {
            throw new NullPointerException("SocketChannel is null ！[" + clientId + "]");
        }

        ConnectionUserInfo dexUserInfo = SessionSocketHolder.getUserInfo(clientId);
        if (null == dexUserInfo) {
            throw new NullPointerException("DexUserInfo is null ！[" + clientId + "]");
        }

        if (System.currentTimeMillis() - dexUserInfo.getLastPing() > ONW_MINUTE) {
            LOGGER.error("The interval between heartbeats is too long ！,token:{}", clientId);
            return;
        }

        DexProto.DexProtocol protocol = DexProto.DexProtocol.newBuilder()
                .setClientId(clientId)
                .setTopics(eventKey)
                .setOpType(DexProtocolValidator.OpType.push.getName())
                .setErrorCode(ErrorCode.normal.getCode())
                .setMessage(message)
                .build();

        ChannelFuture future = socketChannel.writeAndFlush(protocol);
        future.addListener(
                (ChannelFutureListener) channelFuture -> LOGGER.info("服务端成功发送 clientId:{},message:{}", clientId, message));
    }
}
