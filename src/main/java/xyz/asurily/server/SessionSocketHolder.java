package xyz.asurily.server;

import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.asurily.bean.pojo.ConnectionUserInfo;
import xyz.asurily.server.valid.DexProtocolValidator;
import xyz.asurily.server.valid.ValidResult;
import xyz.asurily.server.valid.ValidResultBuilder;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SessionSocketHolder {

    private static final Logger logger = LoggerFactory.getLogger(SessionSocketHolder.class);

    public static final ConcurrentHashMap<String, NioSocketChannel> client2socketMap = new ConcurrentHashMap<>(200);
    public static final ConcurrentHashMap<String, ConnectionUserInfo> clientSubMap = new ConcurrentHashMap<>(200);
    public static final ConcurrentHashMap<String, Set<String>> eventClientMap = new ConcurrentHashMap<>(200);
    public static final LinkedBlockingQueue<ValidResult> onlineEventQueue = new LinkedBlockingQueue(10000);

    public static ConnectionUserInfo getUserInfo(String clientId) {
        return clientSubMap.get(clientId);
    }

    public static NioSocketChannel getSocket(String clientId) {
        return client2socketMap.get(clientId);
    }

    public static void online(NioSocketChannel socketChannel, ValidResult result) {
        client2socketMap.put(result.getClientId(), socketChannel);
        onlineEventQueue.add(result);
    }

    public static void offline(NioSocketChannel nioSocketChannel) {
        client2socketMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isShutdown())
                .forEach(entry -> {
                    onlineEventQueue.add(ValidResultBuilder.build().setOpType(DexProtocolValidator.OpType.logout).setClientId(entry.getKey()).get());
                });
    }
}
