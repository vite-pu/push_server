package xyz.asurily.server;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.Map;

public class ChannelInfo {

    public static final AttributeKey<ChannelInfo> ATTRIBUTE_KEY = AttributeKey.valueOf("CI");

    private String clientIp;

    private Map<String, Object> attr;

    public ChannelInfo() {
        attr = new HashMap<>();
    }

    public void init(Channel channel) throws Exception {
        if (channel != null) {
            //将ChannelInfo绑定到Channel中
            channel.attr(ChannelInfo.ATTRIBUTE_KEY).set(this);
        } else {
            throw new Exception("Channel is null!");
        }
    }

    public static ChannelInfo getChannelInfo(Channel channel) {
        return (channel == null) ? null
                : channel.attr(ChannelInfo.ATTRIBUTE_KEY).get();
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Map<String, Object> getAttr() {
        return attr;
    }

    public void setAttr(Map<String, Object> attr) {
        this.attr = attr;
    }

    public void addAttr(String key, Object val) {
        this.attr.put(key, val);
    }

    public Object removeAttr(String key) {
        return this.attr.remove(key);
    }

}
