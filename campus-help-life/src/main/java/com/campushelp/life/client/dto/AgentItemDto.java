package com.campushelp.life.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentItemDto {
    private Long id;
    private Long sellerUserId;
    private Long campusId;
    private String title;
    private String description;
    private Integer priceCent;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
