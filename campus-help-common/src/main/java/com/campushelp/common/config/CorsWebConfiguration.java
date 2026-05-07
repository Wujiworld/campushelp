package com.campushelp.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * V4：浏览器跨域（联调 Vite 开发服务器等）。与 Spring Security {@code http.cors()} 配合使用。
 * <p>
 * 允许来源通过 {@code campus.cors.allowed-origins} 配置，逗号分隔。
 */
@Configuration
public class CorsWebConfiguration {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${campus.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}") String allowedOrigins) {
        CorsConfiguration c = new CorsConfiguration();
        List<String> patterns = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        c.setAllowedOriginPatterns(patterns);
        c.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(List.of("X-Request-Id", "X-Api-Version", "Authorization"));
        c.setAllowCredentials(true);
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", c);
        source.registerCorsConfiguration("/actuator/**", c);
        return source;
    }
}
