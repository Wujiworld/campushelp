package com.campushelp.common.config;

import com.campushelp.common.web.RequestIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * 注册跨切面过滤器（不将 {@link RequestIdFilter} 声明为 {@code @Component}，
 * 避免与仅依赖 common 部分包的应用误扫到 security 相关 Bean）。
 */
@Configuration
public class CommonWebConfiguration {

    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilterRegistration() {
        FilterRegistrationBean<RequestIdFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new RequestIdFilter());
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        reg.setName("requestIdFilter");
        return reg;
    }
}
