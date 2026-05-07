package com.campushelp.life.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentView {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
}
