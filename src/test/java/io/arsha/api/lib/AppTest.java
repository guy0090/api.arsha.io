package io.arsha.api.lib;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties
@Retention(RetentionPolicy.RUNTIME)
public @interface AppTest {}
