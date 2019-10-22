package xyz.asurily.bean.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;


@Setter
@Getter
@ToString
@AllArgsConstructor
public class ConnectionUserInfo {
    private String clientId;
    private long lastPing;
    private String ip;
    private Map<String, Long> eventSubMap;
}
