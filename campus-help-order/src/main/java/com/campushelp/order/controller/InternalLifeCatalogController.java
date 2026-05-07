package com.campushelp.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.order.entity.ChAgentItem;
import com.campushelp.order.entity.ChOrderItem;
import com.campushelp.order.entity.ChSecondhandImage;
import com.campushelp.order.entity.ChSecondhandItem;
import com.campushelp.order.mapper.ChAgentItemMapper;
import com.campushelp.order.mapper.ChOrderItemMapper;
import com.campushelp.order.mapper.ChSecondhandImageMapper;
import com.campushelp.order.mapper.ChSecondhandItemMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class InternalLifeCatalogController {

    private final ChAgentItemMapper agentItemMapper;
    private final ChSecondhandItemMapper secondhandItemMapper;
    private final ChSecondhandImageMapper secondhandImageMapper;
    private final ChOrderItemMapper orderItemMapper;

    public InternalLifeCatalogController(ChAgentItemMapper agentItemMapper,
                                         ChSecondhandItemMapper secondhandItemMapper,
                                         ChSecondhandImageMapper secondhandImageMapper,
                                         ChOrderItemMapper orderItemMapper) {
        this.agentItemMapper = agentItemMapper;
        this.secondhandItemMapper = secondhandItemMapper;
        this.secondhandImageMapper = secondhandImageMapper;
        this.orderItemMapper = orderItemMapper;
    }

    @GetMapping("/api/v3/internal/life/agent/items")
    public List<ChAgentItem> listAgentItems(@RequestParam(value = "campusId", required = false) Long campusId,
                                            @RequestParam(value = "status", required = false) String status) {
        QueryWrapper<ChAgentItem> q = new QueryWrapper<>();
        if (campusId != null) q.eq("campus_id", campusId);
        if (status != null && !status.isBlank()) q.eq("status", status.trim().toUpperCase());
        q.orderByDesc("created_at").last("LIMIT 200");
        return agentItemMapper.selectList(q);
    }

    @GetMapping("/api/v3/internal/life/agent/items/{id}")
    public ChAgentItem getAgentItem(@PathVariable("id") Long id) {
        return agentItemMapper.selectById(id);
    }

    @PostMapping("/api/v3/internal/life/agent/items")
    public ChAgentItem createAgentItem(@RequestParam("sellerUserId") Long sellerUserId,
                                       @RequestParam("campusId") Long campusId,
                                       @RequestParam("title") String title,
                                       @RequestParam(value = "description", required = false) String description,
                                       @RequestParam("priceCent") Integer priceCent) {
        LocalDateTime now = LocalDateTime.now();
        ChAgentItem it = new ChAgentItem();
        it.setSellerUserId(sellerUserId);
        it.setCampusId(campusId);
        it.setTitle(title);
        it.setDescription(description);
        it.setPriceCent(priceCent);
        it.setStatus("ON_SALE");
        it.setCreatedAt(now);
        it.setUpdatedAt(now);
        agentItemMapper.insert(it);
        return it;
    }

    @PostMapping("/api/v3/internal/life/agent/items/{id}/offline")
    public Boolean offlineAgentItem(@PathVariable("id") Long id) {
        return agentItemMapper.update(null, new UpdateWrapper<ChAgentItem>()
                .eq("id", id)
                .set("status", "OFFLINE")
                .set("updated_at", LocalDateTime.now())) > 0;
    }

    @PostMapping("/api/v3/internal/life/agent/items/{id}/restore")
    public Boolean restoreAgentItem(@PathVariable("id") Long id) {
        return agentItemMapper.update(null, new UpdateWrapper<ChAgentItem>()
                .eq("id", id)
                .eq("status", "OFFLINE")
                .set("status", "ON_SALE")
                .set("updated_at", LocalDateTime.now())) > 0;
    }

    @GetMapping("/api/v3/internal/life/orders/{orderId}/agent-line")
    public ChOrderItem getAgentOrderLine(@PathVariable("orderId") Long orderId) {
        return orderItemMapper.selectOne(new QueryWrapper<ChOrderItem>()
                .eq("order_id", orderId)
                .eq("item_type", "AGENT")
                .last("LIMIT 1"));
    }

    @GetMapping("/api/v3/internal/life/secondhand/items")
    public List<ChSecondhandItem> listSecondhandItems(@RequestParam(value = "campusId", required = false) Long campusId,
                                                       @RequestParam(value = "status", required = false) String status,
                                                       @RequestParam(value = "keyword", required = false) String keyword,
                                                       @RequestParam(value = "minPriceCent", required = false) Integer minPriceCent,
                                                       @RequestParam(value = "maxPriceCent", required = false) Integer maxPriceCent,
                                                       @RequestParam(value = "sort", required = false) String sort) {
        QueryWrapper<ChSecondhandItem> q = new QueryWrapper<>();
        if (campusId != null) q.eq("campus_id", campusId);
        if (status != null && !status.isBlank()) q.eq("status", status.trim().toUpperCase());
        if (keyword != null && !keyword.isBlank()) q.like("title", keyword.trim());
        if (minPriceCent != null) q.ge("price_cent", minPriceCent);
        if (maxPriceCent != null) q.le("price_cent", maxPriceCent);
        String s = sort == null ? "NEWEST" : sort.trim().toUpperCase();
        if ("PRICE_ASC".equals(s)) q.orderByAsc("price_cent").orderByDesc("created_at");
        else if ("PRICE_DESC".equals(s)) q.orderByDesc("price_cent").orderByDesc("created_at");
        else q.orderByDesc("created_at");
        q.last("LIMIT 200");
        return secondhandItemMapper.selectList(q);
    }

    @GetMapping("/api/v3/internal/life/secondhand/items/{id}")
    public ChSecondhandItem getSecondhandItem(@PathVariable("id") Long id) {
        return secondhandItemMapper.selectById(id);
    }

    @GetMapping("/api/v3/internal/life/secondhand/items/{id}/images")
    public List<ChSecondhandImage> listSecondhandImages(@PathVariable("id") Long itemId) {
        return secondhandImageMapper.selectList(new QueryWrapper<ChSecondhandImage>()
                .eq("item_id", itemId).orderByAsc("sort_no"));
    }

    @GetMapping("/api/v3/internal/life/secondhand/seller/{sellerUserId}/items")
    public List<ChSecondhandItem> listSecondhandBySeller(@PathVariable("sellerUserId") Long sellerUserId) {
        return secondhandItemMapper.selectList(new QueryWrapper<ChSecondhandItem>()
                .eq("seller_user_id", sellerUserId)
                .orderByDesc("created_at")
                .last("LIMIT 200"));
    }

    @PostMapping("/api/v3/internal/life/secondhand/items")
    @Transactional(rollbackFor = Exception.class)
    public ChSecondhandItem createSecondhandItem(@RequestParam("sellerUserId") Long sellerUserId,
                                                 @RequestParam("campusId") Long campusId,
                                                 @RequestParam("title") String title,
                                                 @RequestParam(value = "description", required = false) String description,
                                                 @RequestParam("priceCent") Integer priceCent,
                                                 @RequestParam("negotiable") Integer negotiable) {
        LocalDateTime now = LocalDateTime.now();
        ChSecondhandItem item = new ChSecondhandItem();
        item.setSellerUserId(sellerUserId);
        item.setCampusId(campusId);
        item.setTitle(title);
        item.setDescription(description);
        item.setPriceCent(priceCent);
        item.setNegotiable(negotiable);
        item.setStatus("ON_SALE");
        item.setLikeCount(0);
        item.setViewCount(0);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        secondhandItemMapper.insert(item);
        return item;
    }

    @PostMapping("/api/v3/internal/life/secondhand/items/{id}/images")
    public ChSecondhandImage addSecondhandImage(@PathVariable("id") Long itemId,
                                                @RequestParam("url") String url,
                                                @RequestParam("sortNo") Integer sortNo) {
        ChSecondhandImage img = new ChSecondhandImage();
        img.setItemId(itemId);
        img.setUrl(url);
        img.setSortNo(sortNo);
        img.setCreatedAt(LocalDateTime.now());
        secondhandImageMapper.insert(img);
        return img;
    }

    @PostMapping("/api/v3/internal/life/secondhand/items/{id}/offline")
    public Boolean offlineSecondhand(@PathVariable("id") Long itemId) {
        return secondhandItemMapper.update(null, new UpdateWrapper<ChSecondhandItem>()
                .eq("id", itemId)
                .set("status", "OFFLINE")
                .set("updated_at", LocalDateTime.now())) > 0;
    }

    @PostMapping("/api/v3/internal/life/secondhand/items/{id}/view")
    public Boolean incSecondhandView(@PathVariable("id") Long itemId) {
        return secondhandItemMapper.update(null, new UpdateWrapper<ChSecondhandItem>()
                .eq("id", itemId)
                .setSql("view_count = IFNULL(view_count, 0) + 1")) > 0;
    }

    @PostMapping("/api/v3/internal/life/secondhand/items/{id}/status")
    public Boolean updateSecondhandStatus(@PathVariable("id") Long itemId,
                                          @RequestParam("fromStatus") String fromStatus,
                                          @RequestParam("toStatus") String toStatus) {
        UpdateWrapper<ChSecondhandItem> u = new UpdateWrapper<>();
        u.eq("id", itemId)
                .eq("status", fromStatus)
                .set("status", toStatus)
                .set("updated_at", LocalDateTime.now());
        return secondhandItemMapper.update(null, u) > 0;
    }
}
