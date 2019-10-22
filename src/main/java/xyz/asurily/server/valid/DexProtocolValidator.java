package xyz.asurily.server.valid;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import xyz.asurily.bean.protocol.DexProto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DexProtocolValidator {

    final static Map<EventType, String> validExpressionMap = new HashMap<>();

    public enum EventType {
        market_kline("market.kline"),
        market_depth("market.depth"),
        market_tickers("market.tickers"),
        market_quoteToken_tickets("market.quoteToken.tickers"),
        market_quoteTokenCategory_tickers("market.quoteTokenCategory.tickers"),
        market_trade("market.trade");


        String name;

        EventType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public static Map<String, EventType> getInstance(String value) {

            Map<String, EventType> eventMap = new HashMap<>(10);

            String[] eventList = value.split(",");

            if (eventList.length == 0) {
                return Collections.EMPTY_MAP;
            }

            for (String topic : eventList) {
                for (Map.Entry<EventType, String> item : validExpressionMap.entrySet()) {
                    if (topic.matches(item.getValue())) {
                        eventMap.put(topic, item.getKey());
                    }
                }
            }

            return eventMap;
        }
    }

    public enum OpType {
        sub("sub"),
        un_sub("un_sub"),
        ping("ping"),
        pong("pong"),
        push("push"),
        logout("logout");

        String name;

        OpType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        static OpType getInstance(String name) {
            switch (name) {
                case "sub":
                    return sub;
                case "un_sub":
                    return un_sub;
                case "ping":
                    return ping;
                case "pong":
                    return pong;
                case "push":
                    return push;
                default:
                    return null;
            }
        }
    }


    static {
        validExpressionMap.put(
                EventType.market_kline,
                "^market.[a-zA-Z0-9-]+_[a-zA-Z0-9-]+.kline.(minute|hour|day|week|month|year|minute15|minute30|hour2|hour4|hour6|hour12|month3|month6)$"
        );

        validExpressionMap.put(
                EventType.market_depth,
                "^market.[a-zA-Z0-9-]+_[a-zA-Z0-9-]+.(depth.step[0-9]+|depth)$"
        );
        validExpressionMap.put(
                EventType.market_tickers,
                "^market.[a-zA-Z0-9-]+_[a-zA-Z0-9-]+.tickers$"
        );
        validExpressionMap.put(
                EventType.market_quoteToken_tickets,
                "^market.quoteToken.[a-zA-Z0-9-]+.tickers$"
        );
        validExpressionMap.put(
                EventType.market_quoteTokenCategory_tickers,
                "^market.quoteTokenCategory.(BTC|ETH|USDT).tickers$"
        );
        validExpressionMap.put(
                EventType.market_trade,
                "^market.[a-zA-Z0-9-]+_[a-zA-Z0-9-]+.trade$"
        );
    }

    public static ValidResult validEventKey(DexProto.DexProtocol dexProtocol) {

        if (StringUtils.isEmpty(dexProtocol.getClientId())) {
            return ValidResultBuilder.build().setErrorCode(ErrorCode.illegal_client_id).get();
        }

        if (StringUtils.isEmpty(dexProtocol.getOpType())) {
            return ValidResultBuilder.build().setClientId(dexProtocol.getClientId()).setErrorCode(ErrorCode.illegal_op_type).get();
        }

        OpType opType = OpType.getInstance(dexProtocol.getOpType());
        if (opType == null) {
            return ValidResultBuilder.build().setClientId(dexProtocol.getClientId()).setErrorCode(ErrorCode.illegal_op_type).get();
        }

        if (OpType.sub == opType || OpType.un_sub == opType) {

            if (StringUtils.isEmpty(dexProtocol.getTopics())) {
                return ValidResultBuilder.build().setClientId(dexProtocol.getClientId()).setErrorCode(ErrorCode.illegal_event_key).get();
            }

            Map<String, EventType> eventMap = EventType.getInstance(dexProtocol.getTopics());
            if (MapUtils.isEmpty(eventMap)) {
                return ValidResultBuilder.build().setClientId(dexProtocol.getClientId()).setErrorCode(ErrorCode.illegal_event_key).get();
            }

            return ValidResultBuilder.build().setClientId(dexProtocol.getClientId()).setOpType(opType).setEventMap(eventMap).setErrorCode(ErrorCode.normal).get();

        }
        return ValidResultBuilder.build().setClientId(dexProtocol.getClientId()).setOpType(opType).setErrorCode(ErrorCode.normal).get();
    }
}
