package xyz.asurily.bean.http;

import lombok.Data;

@Data
public class PushRequest {

    private String topics;

    private byte[] data;
}
