package io.arsha.api.config.properties;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import java.util.Optional;
import lombok.Data;

@Data
public class RedisProperties {
    Integer port = 6379;
    String host = "localhost"; // "redis.service.arsha";
    /// Password for Redis connection, ignored if blank.
    String password = "";

    /// Redisson expects a null password to mean "no password", so we return null if the password is blank.
    @Nullable
    public String getPassword() {
        return  Optional.of(password).filter(StringUtils::isNotBlank).orElse(null);
    }
}
