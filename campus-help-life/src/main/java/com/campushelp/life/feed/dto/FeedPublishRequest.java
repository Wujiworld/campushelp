package com.campushelp.life.feed.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class FeedPublishRequest {
    @NotBlank
    @Size(max = 32)
    private String bizType;
    @NotBlank
    @Size(max = 64)
    private String bizId;
    @NotBlank
    @Size(max = 500)
    private String content;

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
