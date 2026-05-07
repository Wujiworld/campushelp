package com.campushelp.life.client.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SecondhandImageDto {
    private Long id;
    private Long itemId;
    private String url;
    private Integer sortNo;
    private LocalDateTime createdAt;
}
