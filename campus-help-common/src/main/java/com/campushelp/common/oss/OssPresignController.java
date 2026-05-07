package com.campushelp.common.oss;

import com.campushelp.common.security.SecurityContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class OssPresignController {

    private final CampusOssProperties props;
    private final AliyunOssPresignService presignService;

    public OssPresignController(CampusOssProperties props,
                                @Autowired(required = false) AliyunOssPresignService presignService) {
        this.props = props;
        this.presignService = presignService;
    }

    @PostMapping("/api/v3/oss/presign")
    public OssPresignResult presign(@RequestBody(required = false) OssPresignRequest body) {
        SecurityContextUtils.requireUserId();
        if (!props.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OSS 未启用（campus.oss.enabled=false）");
        }
        if (presignService == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OSS 预签名服务不可用");
        }
        OssPresignRequest req = body != null ? body : new OssPresignRequest();
        return presignService.presignPut(req.getContentType(), req.getFileExtension());
    }
}
