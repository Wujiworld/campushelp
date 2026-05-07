package com.campushelp.life.market.service;

import com.campushelp.life.client.LifeCatalogClient;
import com.campushelp.life.client.ProductServiceClient;
import com.campushelp.life.client.dto.AgentItemDto;
import com.campushelp.life.client.dto.ProductDto;
import com.campushelp.life.client.dto.ProductSkuDto;
import com.campushelp.life.client.dto.SecondhandItemDto;
import com.campushelp.life.client.dto.StoreDto;
import com.campushelp.life.market.dto.MarketItemRow;
import com.campushelp.life.market.dto.MarketPageResponse;
import com.campushelp.life.secondhand.service.SecondhandCatalogService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MarketQueryService {

    private final ProductServiceClient productServiceClient;
    private final LifeCatalogClient lifeCatalogClient;
    private final SecondhandCatalogService secondhandCatalogService;
    private final MeterRegistry meterRegistry;
    private final String provider;

    public MarketQueryService(ProductServiceClient productServiceClient,
                              LifeCatalogClient lifeCatalogClient,
                              SecondhandCatalogService secondhandCatalogService,
                              MeterRegistry meterRegistry,
                              @Value("${campus.search.provider:db}") String provider) {
        this.productServiceClient = productServiceClient;
        this.lifeCatalogClient = lifeCatalogClient;
        this.secondhandCatalogService = secondhandCatalogService;
        this.meterRegistry = meterRegistry;
        this.provider = provider;
    }

    public MarketPageResponse search(String type,
                                     String keyword,
                                     Integer minPriceCent,
                                     Integer maxPriceCent,
                                     Long campusId,
                                     int page,
                                     int size) {
        if ("es".equalsIgnoreCase(provider)) {
            meterRegistry.counter("campus.search.query.es").increment();
            // 当前阶段先通过统一入口切换到 ES 模式，若未接入索引可灰度回退 DB 聚合。
            return searchByDatabase(type, keyword, minPriceCent, maxPriceCent, campusId, page, size);
        }
        return searchByDatabase(type, keyword, minPriceCent, maxPriceCent, campusId, page, size);
    }

    private MarketPageResponse searchByDatabase(String type,
                                                String keyword,
                                                Integer minPriceCent,
                                                Integer maxPriceCent,
                                                Long campusId,
                                                int page,
                                                int size) {
        String t = type == null ? "ALL" : type.trim().toUpperCase(Locale.ROOT);
        meterRegistry.counter("campus.search.query.total", "provider", provider).increment();
        List<MarketItemRow> rows = new ArrayList<>();
        if ("ALL".equals(t) || "TAKEOUT".equals(t)) {
            rows.addAll(loadTakeout(campusId, keyword, minPriceCent, maxPriceCent));
        }
        if ("ALL".equals(t) || "SECONDHAND".equals(t)) {
            rows.addAll(loadSecondhand(campusId, keyword, minPriceCent, maxPriceCent));
        }
        if ("ALL".equals(t) || "AGENT".equals(t)) {
            rows.addAll(loadAgent(campusId, keyword, minPriceCent, maxPriceCent));
        }
        rows.sort(Comparator.comparing(MarketItemRow::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        long total = rows.size();
        int from = Math.min(Math.max(0, page) * size, rows.size());
        int to = Math.min(from + size, rows.size());
        List<MarketItemRow> slice = rows.subList(from, to);
        return new MarketPageResponse(slice, total, Math.max(0, page), size);
    }

    private List<MarketItemRow> loadTakeout(Long campusId, String keyword, Integer minPc, Integer maxPc) {
        List<ProductSkuDto> skus = productServiceClient.listSkus(1, 500);
        if (skus.isEmpty()) {
            return List.of();
        }
        Set<Long> pids = skus.stream().map(ProductSkuDto::getProductId).collect(Collectors.toSet());
        Map<Long, ProductDto> pmap = productServiceClient.listProductsByIds(new ArrayList<>(pids)).stream()
                .filter(p -> p.getStatus() != null && p.getStatus() == 1)
                .collect(Collectors.toMap(ProductDto::getId, p -> p));
        Set<Long> sids = pmap.values().stream().map(ProductDto::getStoreId).collect(Collectors.toSet());
        Map<Long, StoreDto> smap = new HashMap<>();
        if (!sids.isEmpty()) {
            for (StoreDto s : productServiceClient.listStoresByIds(new ArrayList<>(sids))) {
                if (s.getStatus() != null && s.getStatus() == 1) {
                    if (campusId == null || campusId.equals(s.getCampusId())) {
                        smap.put(s.getId(), s);
                    }
                }
            }
        }
        List<MarketItemRow> out = new ArrayList<>();
        for (ProductSkuDto sku : skus) {
            ProductDto p = pmap.get(sku.getProductId());
            if (p == null) {
                continue;
            }
            StoreDto st = smap.get(p.getStoreId());
            if (st == null) {
                continue;
            }
            String title = p.getName() + (sku.getSkuName() == null || sku.getSkuName().isBlank() ? "" : " · " + sku.getSkuName());
            if (!matchTitle(title, keyword)) {
                continue;
            }
            if (!matchPrice(sku.getPriceCent(), minPc, maxPc)) {
                continue;
            }
            out.add(new MarketItemRow(
                    "TAKEOUT",
                    sku.getId(),
                    title,
                    sku.getPriceCent(),
                    st.getCampusId(),
                    p.getCoverUrl(),
                    "ON_SALE",
                    sku.getCreatedAt()));
        }
        return out;
    }

    private List<MarketItemRow> loadSecondhand(Long campusId, String keyword, Integer minPc, Integer maxPc) {
        List<SecondhandItemDto> list = lifeCatalogClient.listSecondhandItems(campusId, "ON_SALE", keyword, minPc, maxPc, "NEWEST");
        List<MarketItemRow> out = new ArrayList<>();
        for (SecondhandItemDto it : list) {
            List<String> urls = secondhandCatalogService.imageUrlsOf(it.getId());
            String cover = urls.isEmpty() ? null : urls.get(0);
            out.add(new MarketItemRow(
                    "SECONDHAND",
                    it.getId(),
                    it.getTitle(),
                    it.getPriceCent(),
                    it.getCampusId(),
                    cover,
                    it.getStatus(),
                    it.getCreatedAt()));
        }
        return out;
    }

    private List<MarketItemRow> loadAgent(Long campusId, String keyword, Integer minPc, Integer maxPc) {
        List<AgentItemDto> list = lifeCatalogClient.listAgentItems(campusId, "ON_SALE");
        List<MarketItemRow> out = new ArrayList<>();
        for (AgentItemDto it : list) {
            if (!matchTitle(it.getTitle(), keyword)) {
                continue;
            }
            if (!matchPrice(it.getPriceCent(), minPc, maxPc)) {
                continue;
            }
            out.add(new MarketItemRow(
                    "AGENT",
                    it.getId(),
                    it.getTitle(),
                    it.getPriceCent(),
                    it.getCampusId(),
                    null,
                    it.getStatus(),
                    it.getCreatedAt()));
        }
        return out;
    }

    private static boolean matchTitle(String title, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return title.toLowerCase(Locale.ROOT).contains(keyword.trim().toLowerCase(Locale.ROOT));
    }

    private static boolean matchPrice(int priceCent, Integer minPc, Integer maxPc) {
        if (minPc != null && priceCent < minPc) {
            return false;
        }
        if (maxPc != null && priceCent > maxPc) {
            return false;
        }
        return true;
    }
}
