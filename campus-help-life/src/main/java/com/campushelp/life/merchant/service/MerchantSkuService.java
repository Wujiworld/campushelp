package com.campushelp.life.merchant.service;

import com.campushelp.common.exception.ValidationException;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.client.ProductServiceClient;
import com.campushelp.life.client.dto.ProductDto;
import com.campushelp.life.client.dto.ProductSkuDto;
import com.campushelp.life.client.dto.StoreDto;
import com.campushelp.life.merchant.dto.MerchantSkuCreateRequest;
import com.campushelp.life.merchant.dto.MerchantSkuUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantSkuService {

    private final ProductServiceClient productServiceClient;

    public MerchantSkuService(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    private void assertOwnsProduct(long userId, ProductDto product) {
        if (SecurityContextUtils.hasRole("ADMIN")) {
            return;
        }
        SecurityContextUtils.requireRole("MERCHANT");
        StoreDto store = productServiceClient.getStore(product.getStoreId());
        if (store == null || store.getMerchantUserId() == null || !store.getMerchantUserId().equals(userId)) {
            throw new ValidationException("无权操作该商品");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductSkuDto create(long userId, MerchantSkuCreateRequest req) {
        ProductDto product = productServiceClient.getProduct(req.getProductId());
        if (product == null) {
            throw new ValidationException("商品不存在");
        }
        assertOwnsProduct(userId, product);
        return productServiceClient.createSku(req.getProductId(), req.getSkuName(), req.getPriceCent(), req.getStock());
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductSkuDto update(long userId, long skuId, MerchantSkuUpdateRequest req) {
        ProductSkuDto sku = productServiceClient.getSku(skuId);
        if (sku == null) {
            throw new ValidationException("SKU 不存在");
        }
        ProductDto product = productServiceClient.getProduct(sku.getProductId());
        if (product == null) {
            throw new ValidationException("商品不存在");
        }
        assertOwnsProduct(userId, product);
        String skuName = req.getSkuName();
        if (skuName != null) {
            skuName = skuName.trim().isEmpty() ? null : skuName.trim();
        }
        return productServiceClient.updateSku(skuId, skuName, req.getPriceCent(), req.getStock());
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductSkuDto setStatus(long userId, long skuId, int status) {
        if (status != 0 && status != 1) {
            throw new ValidationException("status 仅支持 0 或 1");
        }
        ProductSkuDto sku = productServiceClient.getSku(skuId);
        if (sku == null) {
            throw new ValidationException("SKU 不存在");
        }
        ProductDto product = productServiceClient.getProduct(sku.getProductId());
        if (product == null) {
            throw new ValidationException("商品不存在");
        }
        assertOwnsProduct(userId, product);
        return productServiceClient.updateSkuStatus(skuId, status);
    }
}
