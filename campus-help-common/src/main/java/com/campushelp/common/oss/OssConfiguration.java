package com.campushelp.common.oss;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CampusOssProperties.class)
public class OssConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "campus.oss", name = "enabled", havingValue = "true")
    public ApplicationRunner ossPropsValidator(CampusOssProperties props) {
        return args -> {
            if (isBlank(props.getEndpoint())
                    || isBlank(props.getAccessKeyId())
                    || isBlank(props.getAccessKeySecret())
                    || isBlank(props.getBucket())) {
                throw new IllegalStateException("OSS 已启用但配置不完整。请设置环境变量："
                        + " OSS_ENDPOINT / OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET / OSS_BUCKET"
                        + "（可选：OSS_PREFIX / OSS_PUBLIC_BASE_URL）");
            }
            String p = props.getPrefix();
            if (p != null && !p.isBlank() && !p.endsWith("/")) {
                props.setPrefix(p + "/");
            }
            String base = props.getPublicBaseUrl();
            if (base != null && base.endsWith("/")) {
                props.setPublicBaseUrl(base.substring(0, base.length() - 1));
            }
        };
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
