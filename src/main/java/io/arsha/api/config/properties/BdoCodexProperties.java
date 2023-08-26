package io.arsha.api.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "codex")
public class BdoCodexProperties {
    private String url = "bdocodex.com";
    private List<String> locales = List.of("en", "de", "fr", "ru", "es", "sp", "jp", "kr", "th", "id", "cn", "tr", "pt", "tw", "gl");
}
