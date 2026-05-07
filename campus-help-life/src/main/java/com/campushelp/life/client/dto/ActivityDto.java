package com.campushelp.life.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityDto {
    private Long id;
    private Long campusId;
    private String title;
    private String description;
    private String place;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Integer likeCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
