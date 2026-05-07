package com.campushelp.life.market.service;

import com.campushelp.common.cache.CacheFacade;
import com.campushelp.life.agent.service.AgentCatalogService;
import com.campushelp.life.client.ProductServiceClient;
import com.campushelp.life.client.dto.AgentItemDto;
import com.campushelp.life.client.dto.ProductDto;
import com.campushelp.life.client.dto.ProductSkuDto;
import com.campushelp.life.client.dto.SecondhandItemDto;
import com.campushelp.life.client.dto.StoreDto;
import com.campushelp.life.secondhand.service.SecondhandCatalogService;
import com.campushelp.common.exception.ValidationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MarketDetailService {

    private final ProductServiceClient productServiceClient;
    private final SecondhandCatalogService secondhandCatalogService;
    private final AgentCatalogService agentCatalogService;
    private final CacheFacade cacheFacade;
    private final ObjectMapper objectMapper;

    public MarketDetailService(ProductServiceClient productServiceClient,
                               SecondhandCatalogService secondhandCatalogService,
                               AgentCatalogService agentCatalogService,
                               CacheFacade cacheFacade,
                               ObjectMapper objectMapper) {
        this.productServiceClient = productServiceClient;
        this.secondhandCatalogService = secondhandCatalogService;
        this.agentCatalogService = agentCatalogService;
        this.cacheFacade = cacheFacade;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> detail(String kind, long id) {
        String k = kind.trim().toUpperCase();
        String cacheKey = "market:detail:" + k + ":" + id;
        String json = cacheFacade.getOrLoadJson(cacheKey, () -> {
            Map<String, Object> data = detailInternal(k, id);
            try {
                return objectMapper.writeValueAsString(data);
            } catch (Exception e) {
                throw new IllegalStateException("serialize market detail failed", e);
            }
        });
        if (json == null) {
            throw new ValidationException("详情不存在");
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() { });
        } catch (Exception e) {
            throw new IllegalStateException("deserialize market detail failed", e);
        }
    }

    private Map<String, Object> detailInternal(String k, long id) {
        switch (k) {
            case "TAKEOUT":
                return takeoutDetail(id);
            case "SECONDHAND":
                return secondhandDetail(id);
            case "AGENT":
                return agentDetail(id);
            default:
                throw new ValidationException("不支持的 kind");
        }
    }

    private Map<String, Object> takeoutDetail(long skuId) {
        ProductSkuDto sku = productServiceClient.getSku(skuId);
        if (sku == null) {
            throw new ValidationException("SKU 不存在");
        }
        ProductDto product = productServiceClient.getProduct(sku.getProductId());
        if (product == null) {
            throw new ValidationException("商品不存在");
        }
        StoreDto store = productServiceClient.getStore(product.getStoreId());
        if (store == null) {
            throw new ValidationException("门店不存在");
        }
        Map<String, Object> m = new HashMap<>();
        m.put("sku", sku);
        m.put("product", product);
        m.put("store", store);
        return m;
    }

    private Map<String, Object> secondhandDetail(long itemId) {
        SecondhandItemDto item = secondhandCatalogService.getById(itemId);
        if (item == null) {
            throw new ValidationException("商品不存在");
        }
        Map<String, Object> m = new HashMap<>();
        m.put("item", item);
        m.put("imageUrls", secondhandCatalogService.imageUrlsOf(itemId));
        return m;
    }

    private Map<String, Object> agentDetail(long itemId) {
        AgentItemDto it = agentCatalogService.getById(itemId);
        if (it == null) {
            throw new ValidationException("条目不存在");
        }
        Map<String, Object> m = new HashMap<>();
        m.put("item", it);
        return m;
    }
}
