package com.campushelp.life.notify.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Map;

@Data
public class SystemBroadcastRequest {
    @NotBlank
    @Size(max = 128)
    private String title;

    @NotBlank
    @Size(max = 512)
    private String content;

    /** 可选：附加展示字段 */
    private Map<String, Object> payload;
}

