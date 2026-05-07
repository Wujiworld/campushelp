package com.campushelp.life.market.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketItemRow {
    private String kind;
    private Long id;
    private String title;
    private Integer priceCent;
    private Long campusId;
    private String coverUrl;
    private String status;
    private LocalDateTime createdAt;
}
