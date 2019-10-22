package xyz.asurily.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Setter
@Getter
@ToString
@Component
public class AppConfig {

    @Value("${ws.server.port}")
    private int wsServerPort;

}
