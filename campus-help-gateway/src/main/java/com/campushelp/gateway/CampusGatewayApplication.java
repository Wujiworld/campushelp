package com.campushelp.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import reactor.core.publisher.Mono;

/**
 * API 网关入口：默认监听 8081，通过服务发现或静态路由转发到各业务微服务。
 */
@SpringBootApplication
public class CampusGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusGatewayApplication.class, args);
    }

    /**
     * 全局限流键：客户端 IP（配合 X-Forwarded-For 时由上游反向代理写入）。
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) {
                return Mono.just(xff.split(",")[0].trim());
            }
            var addr = exchange.getRequest().getRemoteAddress();
            String ip = addr != null ? addr.getAddress().getHostAddress() : "unknown";
            return Mono.just(ip);
        };
    }
}
