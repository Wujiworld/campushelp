package com.campushelp.product.controller;

import com.campushelp.product.entity.ChProduct;
import com.campushelp.product.entity.ChProductSku;
import com.campushelp.product.entity.ChStore;
import com.campushelp.product.mapper.ChProductMapper;
import com.campushelp.product.mapper.ChProductSkuMapper;
import com.campushelp.product.mapper.ChStoreMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    private final ChStoreMapper storeMapper;
    private final ChProductMapper productMapper;
    private final ChProductSkuMapper skuMapper;

    public ProductController(ChStoreMapper storeMapper, ChProductMapper productMapper, ChProductSkuMapper skuMapper) {
        this.storeMapper = storeMapper;
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
    }

    @GetMapping("/api/v3/stores")
    public List<ChStore> stores(@RequestParam("campusId") Long campusId) {
        return storeMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ChStore>()
                        .eq("campus_id", campusId)
                        .eq("status", 1)
        );
    }

    @GetMapping("/api/v3/stores/{storeId}/products")
    public List<ChProduct> products(@PathVariable("storeId") Long storeId) {
        return productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ChProduct>()
                        .eq("store_id", storeId)
                        .eq("status", 1)
        );
    }

    @GetMapping("/api/v3/products/{productId}/skus")
    public List<ChProductSku> skus(@PathVariable("productId") Long productId) {
        return skuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ChProductSku>()
                        .eq("product_id", productId)
                        .eq("status", 1)
        );
    }
}

