package com.campushelp.life.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketTypeDto {
    private Long id;
    private Long activityId;
    private String name;
    private Integer priceCent;
    private Integer stockTotal;
    private Integer stockSold;
    private Integer perUserLimit;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
