package com.campushelp.common.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "campus.oss")
public class CampusOssProperties {
    private boolean enabled;
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucket;
    private String prefix = "";
    private int expireSeconds = 600;
    /** 不含尾斜杠，如 https://bucket.oss-cn-hangzhou.aliyuncs.com */
    private String publicBaseUrl;
}
