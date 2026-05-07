package com.campushelp.life.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreDto {
    private Long id;
    private Long campusId;
    private String name;
    private String address;
    private Integer status;
    private Long merchantUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
