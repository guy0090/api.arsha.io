package io.arsha.api.config.properties;

import lombok.Data;

@Data
public class RedisProperties {
    Integer port = 6379;
    String host = "localhost"; // "redis.service.arsha";
    boolean useAuth = false;
    String password = "";
    String username = "";
}
