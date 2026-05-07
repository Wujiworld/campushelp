package com.campushelp.life.client.dto;

import lombok.Data;

@Data
public class OrderItemDto {
    private Long id;
    private Long orderId;
    private String itemType;
    private Long refId;
    private Integer quantity;
}
