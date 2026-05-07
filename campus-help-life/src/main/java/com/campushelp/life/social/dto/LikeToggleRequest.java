package com.campushelp.life.social.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class LikeToggleRequest {

    @NotBlank
    private String targetType;

    @NotNull
    private Long targetId;
}
