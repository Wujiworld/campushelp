package com.campushelp.life.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSummaryDto {
    private Long id;
    private Long userId;
    private String bizNo;
    private String orderType;
    private String bizType;
    private Integer amountCent;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
