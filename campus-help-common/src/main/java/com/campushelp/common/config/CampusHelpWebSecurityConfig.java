package com.campushelp.common.config;

import com.campushelp.common.safety.ApiSignatureFilter;
import com.campushelp.common.security.ApiJsonAuthenticationEntryPoint;
import com.campushelp.common.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 单体部署下的统一安全策略：公开读接口、注册登录；其余需 JWT。
 * <p>
 * 原各子模块中的 Security 配置已合并到此，避免多 {@link SecurityFilterChain} 冲突。
 */
@Configuration
@EnableWebSecurity
public class CampusHelpWebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain campusHelpSecurityFilterChain(HttpSecurity http,
                                                             JwtAuthenticationFilter jwtAuthenticationFilter,
                                                             ApiJsonAuthenticationEntryPoint apiJsonAuthenticationEntryPoint,
                                                             @Autowired(required = false) ApiSignatureFilter apiSignatureFilter) throws Exception {
        http.cors().and();
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        if (apiSignatureFilter != null) {
            http.addFilterBefore(apiSignatureFilter, JwtAuthenticationFilter.class);
        }
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.authorizeRequests()
                .antMatchers("/actuator/**", "/api/v3/ping").permitAll()
                .antMatchers("/ws/**", "/ws").permitAll()
                .antMatchers("/api/v3/auth/register", "/api/v3/auth/login").permitAll()
                .antMatchers(org.springframework.http.HttpMethod.POST, "/api/v3/payments/mock-callback").permitAll()
                .antMatchers(HttpMethod.GET,
                        "/api/v3/campuses/**",
                        "/api/v3/stores/**",
                        "/api/v3/products/**",
                        "/api/v3/secondhand/items",
                        "/api/v3/secondhand/items/*",
                        "/api/v3/activities",
                        "/api/v3/activities/*",
                        "/api/v3/activities/*/tickets",
                        "/api/v3/likes/state",
                        "/api/v3/market/**",
                        "/api/v3/agent/items",
                        "/api/v3/agent/items/*",
                        "/api/v3/comments").permitAll()
                .anyRequest().authenticated();
        http.exceptionHandling()
                .authenticationEntryPoint(apiJsonAuthenticationEntryPoint);
        return http.build();
    }
}
