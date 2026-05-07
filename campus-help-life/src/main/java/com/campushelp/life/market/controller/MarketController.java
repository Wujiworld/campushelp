package com.campushelp.life.market.controller;

import com.campushelp.life.market.dto.MarketPageResponse;
import com.campushelp.life.market.service.MarketDetailService;
import com.campushelp.life.market.service.MarketQueryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@RestController
public class MarketController {

    private final MarketQueryService marketQueryService;
    private final MarketDetailService marketDetailService;

    public MarketController(MarketQueryService marketQueryService, MarketDetailService marketDetailService) {
        this.marketQueryService = marketQueryService;
        this.marketDetailService = marketDetailService;
    }

    @GetMapping("/api/v3/market/items")
    public MarketPageResponse list(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minPriceCent,
            @RequestParam(required = false) Integer maxPriceCent,
            @RequestParam(required = false) Long campusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int sz = Math.min(Math.max(1, size), 100);
        return marketQueryService.search(type, keyword, minPriceCent, maxPriceCent, campusId, page, sz);
    }

    @GetMapping("/api/v3/market/items/{kind}/{id}")
    public Map<String, Object> detail(@PathVariable String kind, @PathVariable long id) {
        return marketDetailService.detail(kind, id);
    }
}
