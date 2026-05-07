package com.campushelp.life.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MyCommentView {
    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    private String content;
    private LocalDateTime createdAt;
}

