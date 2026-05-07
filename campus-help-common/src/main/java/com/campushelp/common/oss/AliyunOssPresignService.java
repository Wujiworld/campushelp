package com.campushelp.common.oss;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "campus.oss", name = "enabled", havingValue = "true")
public class AliyunOssPresignService {

    private final CampusOssProperties props;

    public AliyunOssPresignService(CampusOssProperties props) {
        this.props = props;
    }

    public OssPresignResult presignPut(String contentType, String suggestedExtension) {
        String ext = suggestedExtension == null || suggestedExtension.isBlank() ? "jpg" : suggestedExtension.replace(".", "");
        String key = (props.getPrefix() == null ? "" : props.getPrefix()) + UUID.randomUUID() + "." + ext;
        long expireMs = Math.max(60, props.getExpireSeconds()) * 1000L;
        Date expiration = new Date(System.currentTimeMillis() + expireMs);

        OSS client = new OSSClientBuilder().build(
                props.getEndpoint(),
                props.getAccessKeyId(),
                props.getAccessKeySecret()
        );
        try {
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(props.getBucket(), key, HttpMethod.PUT);
            req.setExpiration(expiration);
            if (contentType != null && !contentType.isBlank()) {
                req.setContentType(contentType);
            }
            URL url = client.generatePresignedUrl(req);
            String objectUrl = buildObjectUrl(key);
            return new OssPresignResult(
                    url.toString(),
                    key,
                    objectUrl,
                    expiration.getTime() / 1000
            );
        } finally {
            client.shutdown();
        }
    }

    private String buildObjectUrl(String key) {
        String base = props.getPublicBaseUrl();
        if (base == null || base.isBlank()) {
            String ep = props.getEndpoint();
            if (ep != null && ep.startsWith("https://")) {
                ep = ep.substring("https://".length());
            } else if (ep != null && ep.startsWith("http://")) {
                ep = ep.substring("http://".length());
            }
            base = "https://" + props.getBucket() + "." + ep;
        }
        if (base.endsWith("/")) {
            return base + key;
        }
        return base + "/" + key;
    }
}
