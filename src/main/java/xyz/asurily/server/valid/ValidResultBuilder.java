package xyz.asurily.server.valid;


import java.util.Map;

public class ValidResultBuilder {

    private ValidResult validResult;

    public static ValidResultBuilder build() {

        ValidResultBuilder validResultBuilder = new ValidResultBuilder();
        validResultBuilder.validResult = new ValidResult();
        return validResultBuilder;
    }

    public ValidResultBuilder setClientId(String clientId) {
        this.validResult.setClientId(clientId);
        return this;
    }

    public ValidResultBuilder setErrorCode(ErrorCode errorCode) {
        this.validResult.setErrorCode(errorCode);
        return this;
    }

    public ValidResultBuilder setOpType(DexProtocolValidator.OpType opType) {
        this.validResult.setOpType(opType);
        return this;
    }

    public ValidResultBuilder setEventMap(Map<String, DexProtocolValidator.EventType> eventMap) {
        this.validResult.eventMap = eventMap;
        return this;
    }

    public ValidResult get() {
        return this.validResult;
    }

}
