package io.arsha.api.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate buildRestTemplate() {
        var requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofSeconds(5))
                .build();
        var client = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(requestConfig)
                .build();
        var clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(client);
        return new RestTemplate(clientHttpRequestFactory);
    }
}
