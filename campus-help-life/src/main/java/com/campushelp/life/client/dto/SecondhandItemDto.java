package com.campushelp.life.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SecondhandItemDto {
    private Long id;
    private Long sellerUserId;
    private Long campusId;
    private String title;
    private String description;
    private Integer priceCent;
    private Integer negotiable;
    private String status;
    private Integer likeCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
