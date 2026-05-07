package com.campushelp.life.comment.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CommentCreateRequest {

    @NotBlank
    @Size(max = 32)
    private String targetType;

    @NotNull
    private Long targetId;

    @NotBlank
    @Size(max = 512)
    private String content;
}
