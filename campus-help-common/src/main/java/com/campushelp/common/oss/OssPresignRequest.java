package com.campushelp.common.oss;

import lombok.Data;

@Data
public class OssPresignRequest {
    /** 如 image/jpeg */
    private String contentType;
    /** 不含点，如 png */
    private String fileExtension;
}
