package com.campushelp.life.market.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketPageResponse {
    private List<MarketItemRow> content;
    private long total;
    private int page;
    private int size;
}
