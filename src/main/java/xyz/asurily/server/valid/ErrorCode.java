package xyz.asurily.server.valid;

public enum ErrorCode {
    normal(0),
    illegal_client_id(1),
    illegal_event_key(2),
    illegal_op_type(3),
    link(3),
    ;

    int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
