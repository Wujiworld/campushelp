package com.campushelp.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campushelp.product.entity.ChProduct;
import com.campushelp.product.entity.ChProductSku;
import com.campushelp.product.entity.ChStore;
import com.campushelp.product.mapper.ChProductMapper;
import com.campushelp.product.mapper.ChProductSkuMapper;
import com.campushelp.product.mapper.ChStoreMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ProductInternalController {

    private final ChStoreMapper storeMapper;
    private final ChProductMapper productMapper;
    private final ChProductSkuMapper skuMapper;

    public ProductInternalController(ChStoreMapper storeMapper, ChProductMapper productMapper, ChProductSkuMapper skuMapper) {
        this.storeMapper = storeMapper;
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
    }

    @GetMapping("/api/v3/internal/products/stores/{id}")
    public ChStore getStore(@PathVariable("id") Long id) {
        return storeMapper.selectById(id);
    }

    @GetMapping("/api/v3/internal/products/{id}")
    public ChProduct getProduct(@PathVariable("id") Long id) {
        return productMapper.selectById(id);
    }

    @GetMapping("/api/v3/internal/products/skus/{id}")
    public ChProductSku getSku(@PathVariable("id") Long id) {
        return skuMapper.selectById(id);
    }

    @GetMapping("/api/v3/internal/products/skus")
    public List<ChProductSku> listSkus(@RequestParam(value = "status", required = false) Integer status,
                                       @RequestParam(value = "limit", required = false) Integer limit) {
        QueryWrapper<ChProductSku> q = new QueryWrapper<>();
        if (status != null) q.eq("status", status);
        q.orderByDesc("created_at");
        q.last("LIMIT " + Math.max(1, Math.min(limit == null ? 200 : limit, 1000)));
        return skuMapper.selectList(q);
    }

    @GetMapping("/api/v3/internal/products/batch")
    public List<ChProduct> listProductsByIds(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return productMapper.selectBatchIds(ids);
    }

    @GetMapping("/api/v3/internal/products/stores/batch")
    public List<ChStore> listStoresByIds(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return storeMapper.selectBatchIds(ids);
    }

    @GetMapping("/api/v3/internal/products/stores/by-merchant")
    public List<ChStore> storesByMerchant(@RequestParam("merchantUserId") Long merchantUserId) {
        return storeMapper.selectList(new QueryWrapper<ChStore>().eq("merchant_user_id", merchantUserId));
    }

    @PostMapping("/api/v3/internal/products/skus")
    public ChProductSku createSku(@RequestParam("productId") Long productId,
                                  @RequestParam(value = "skuName", required = false) String skuName,
                                  @RequestParam("priceCent") Integer priceCent,
                                  @RequestParam("stock") Integer stock) {
        ChProductSku sku = new ChProductSku();
        sku.setProductId(productId);
        sku.setSkuName(skuName);
        sku.setPriceCent(priceCent);
        sku.setStock(stock);
        sku.setSoldCount(0);
        sku.setStatus(1);
        sku.setCreatedAt(LocalDateTime.now());
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.insert(sku);
        return sku;
    }

    @PutMapping("/api/v3/internal/products/skus/{id}")
    public ChProductSku updateSku(@PathVariable("id") Long id,
                                  @RequestParam(value = "skuName", required = false) String skuName,
                                  @RequestParam(value = "priceCent", required = false) Integer priceCent,
                                  @RequestParam(value = "stock", required = false) Integer stock) {
        ChProductSku sku = skuMapper.selectById(id);
        if (sku == null) return null;
        if (skuName != null) sku.setSkuName(skuName);
        if (priceCent != null) sku.setPriceCent(priceCent);
        if (stock != null) sku.setStock(stock);
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.updateById(sku);
        return sku;
    }

    @PostMapping("/api/v3/internal/products/skus/{id}/status")
    public ChProductSku updateSkuStatus(@PathVariable("id") Long id,
                                        @RequestParam("status") Integer status) {
        skuMapper.update(null, new UpdateWrapper<ChProductSku>()
                .eq("id", id)
                .set("status", status)
                .set("updated_at", LocalDateTime.now()));
        return skuMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/v3/internal/products/skus/{id}/deduct")
    public Boolean deductSkuStock(@PathVariable("id") Long skuId, @RequestParam("quantity") Integer quantity) {
        int rows = skuMapper.update(null, new UpdateWrapper<ChProductSku>()
                .eq("id", skuId)
                .ge("stock", quantity)
                .setSql("stock = stock - " + quantity)
                .set("updated_at", LocalDateTime.now()));
        return rows == 1;
    }

    @PostMapping("/api/v3/internal/products/skus/{id}/restore")
    public Boolean restoreSkuStock(@PathVariable("id") Long skuId, @RequestParam("quantity") Integer quantity) {
        int qty = Math.max(1, quantity == null ? 1 : quantity);
        int rows = skuMapper.update(null, new UpdateWrapper<ChProductSku>()
                .eq("id", skuId)
                .setSql("stock = stock + " + qty)
                .set("updated_at", LocalDateTime.now()));
        return rows == 1;
    }
}
