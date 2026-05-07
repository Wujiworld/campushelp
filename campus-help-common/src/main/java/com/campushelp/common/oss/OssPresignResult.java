package com.campushelp.common.oss;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OssPresignResult {
    private String uploadUrl;
    private String objectKey;
    private String objectUrl;
    private long expireAtEpochSeconds;
}
