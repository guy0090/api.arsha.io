package io.arsha.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityFilterConfiguration {
    // FIXME: move to non deprecated version
    @Bean
    public SecurityFilterChain securityConfig(HttpSecurity http) throws Exception {
        http.securityMatcher("/**").csrf().disable().cors().and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeHttpRequests()
                .anyRequest().permitAll();

        return http.build();
    }
}
