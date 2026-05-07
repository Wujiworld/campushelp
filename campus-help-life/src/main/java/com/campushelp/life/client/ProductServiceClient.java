package com.campushelp.life.client;

import com.campushelp.life.client.dto.ProductDto;
import com.campushelp.life.client.dto.ProductSkuDto;
import com.campushelp.life.client.dto.StoreDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "campus-help-product")
public interface ProductServiceClient {

    @GetMapping("/api/v3/internal/products/stores/{id}")
    StoreDto getStore(@PathVariable("id") Long id);

    @GetMapping("/api/v3/internal/products/{id}")
    ProductDto getProduct(@PathVariable("id") Long id);

    @GetMapping("/api/v3/internal/products/skus/{id}")
    ProductSkuDto getSku(@PathVariable("id") Long id);

    @GetMapping("/api/v3/internal/products/skus")
    List<ProductSkuDto> listSkus(@RequestParam(value = "status", required = false) Integer status,
                                 @RequestParam(value = "limit", required = false) Integer limit);

    @GetMapping("/api/v3/internal/products/batch")
    List<ProductDto> listProductsByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/api/v3/internal/products/stores/batch")
    List<StoreDto> listStoresByIds(@RequestParam("ids") List<Long> ids);

    @PostMapping("/api/v3/internal/products/skus")
    ProductSkuDto createSku(@RequestParam("productId") Long productId,
                            @RequestParam(value = "skuName", required = false) String skuName,
                            @RequestParam("priceCent") Integer priceCent,
                            @RequestParam("stock") Integer stock);

    @PutMapping("/api/v3/internal/products/skus/{id}")
    ProductSkuDto updateSku(@PathVariable("id") Long id,
                            @RequestParam(value = "skuName", required = false) String skuName,
                            @RequestParam(value = "priceCent", required = false) Integer priceCent,
                            @RequestParam(value = "stock", required = false) Integer stock);

    @PostMapping("/api/v3/internal/products/skus/{id}/status")
    ProductSkuDto updateSkuStatus(@PathVariable("id") Long id,
                                  @RequestParam("status") Integer status);
}
