package xyz.asurily.bean.protocol;

import com.google.protobuf.InvalidProtocolBufferException;

public class ProtocolUtil {

    public static void main(String[] args) throws InvalidProtocolBufferException {
        DexProto.DexProtocol protocol = DexProto.DexProtocol.newBuilder()
                .setTopics("btc")
                .build();

        byte[] encode = encode(protocol);

        DexProto.DexProtocol parseFrom = decode(encode);

        System.out.println(protocol.toString());
        System.out.println(protocol.toString().equals(parseFrom.toString()));
    }

    /**
     * 编码
     *
     * @param protocol
     * @return
     */
    public static byte[] encode(DexProto.DexProtocol protocol) {
        return protocol.toByteArray();
    }

    /**
     * 解码
     *
     * @param bytes
     * @return
     * @throws InvalidProtocolBufferException
     */
    public static DexProto.DexProtocol decode(byte[] bytes) throws InvalidProtocolBufferException {
        return DexProto.DexProtocol.parseFrom(bytes);
    }
}
