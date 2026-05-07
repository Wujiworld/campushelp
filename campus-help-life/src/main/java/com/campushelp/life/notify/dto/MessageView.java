package com.campushelp.life.notify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class MessageView {
    private Long id;
    private String type;
    private String title;
    private String content;
    private Map<String, Object> payload;
    private LocalDateTime createdAt;
    /** null 表示未读 */
    private LocalDateTime readAt;
}

