package xyz.asurily.server.valid;

import lombok.Data;

import java.util.Map;

@Data
public class ValidResult {

    String clientId;

    String clientIp;

    Map<String, DexProtocolValidator.EventType> eventMap;

    DexProtocolValidator.OpType opType;

    ErrorCode errorCode;

}
