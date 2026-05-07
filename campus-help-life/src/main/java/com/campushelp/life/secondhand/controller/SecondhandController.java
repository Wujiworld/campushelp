package com.campushelp.life.secondhand.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.common.exception.ValidationException;
import com.campushelp.life.client.OrderServiceClient;
import com.campushelp.life.client.dto.OrderSummaryDto;
import com.campushelp.life.client.dto.SecondhandItemDto;
import com.campushelp.life.secondhand.dto.SecondhandItemCreateRequest;
import com.campushelp.life.secondhand.dto.SecondhandPurchaseRequest;
import com.campushelp.life.secondhand.service.SecondhandCatalogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Validated
@RestController
public class SecondhandController {

    private final SecondhandCatalogService catalogService;
    private final OrderServiceClient orderServiceClient;

    public SecondhandController(SecondhandCatalogService catalogService, OrderServiceClient orderServiceClient) {
        this.catalogService = catalogService;
        this.orderServiceClient = orderServiceClient;
    }

    @GetMapping("/api/v3/secondhand/items")
    public List<Map<String, Object>> listItems(
            @RequestParam(required = false) Long campusId,
            @RequestParam(defaultValue = "ON_SALE") String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minPriceCent,
            @RequestParam(required = false) Integer maxPriceCent,
            @RequestParam(required = false) String sort) {
        return catalogService.list(campusId, status, keyword, minPriceCent, maxPriceCent, sort).stream()
                .map(catalogService::toBriefMap)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3/secondhand/items/{id}")
    public Map<String, Object> getItem(@PathVariable Long id) {
        SecondhandItemDto item = catalogService.getById(id);
        if (item == null) {
            throw new ValidationException("商品不存在");
        }
        catalogService.recordView(id);
        item = catalogService.getById(id);
        Map<String, Object> m = new HashMap<>();
        m.put("item", item);
        m.put("imageUrls", catalogService.imageUrlsOf(id));
        return m;
    }

    @GetMapping("/api/v3/secondhand/my/items")
    public List<Map<String, Object>> myItems() {
        Long uid = SecurityContextUtils.requireUserId();
        return catalogService.listBySeller(uid).stream().map(catalogService::toBriefMap).collect(Collectors.toList());
    }

    @PostMapping("/api/v3/secondhand/items")
    public SecondhandItemDto publish(@Valid @RequestBody SecondhandItemCreateRequest req,
                                     @RequestParam Long campusId) {
        Long uid = SecurityContextUtils.requireUserId();
        if (SecurityContextUtils.hasRole("RIDER") && !SecurityContextUtils.hasRole("ADMIN")) {
            throw new ValidationException("骑手账号不可发布闲置");
        }
        return catalogService.publish(uid, campusId, req);
    }

    @PostMapping("/api/v3/secondhand/items/{id}/offline")
    public void offline(@PathVariable Long id) {
        catalogService.offline(SecurityContextUtils.requireUserId(), id);
    }

    @PostMapping("/api/v3/secondhand/orders")
    public OrderSummaryDto purchase(@Valid @RequestBody SecondhandPurchaseRequest req) {
        Long uid = SecurityContextUtils.requireUserId();
        return orderServiceClient.createSecondhandPurchase(
                uid,
                req.getCampusId(),
                req.getItemId(),
                req.getDeliveryMode(),
                req.getAddressId());
    }

}
