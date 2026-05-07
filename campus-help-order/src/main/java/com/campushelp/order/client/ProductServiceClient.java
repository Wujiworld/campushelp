package com.campushelp.order.client;

import com.campushelp.common.api.ApiResult;
import com.campushelp.order.client.dto.ProductDTO;
import com.campushelp.order.client.dto.ProductSkuDTO;
import com.campushelp.order.client.dto.StoreDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "campus-help-product", url = "${product.service.base-url:}")
public interface ProductServiceClient {

    @GetMapping("/api/v3/internal/products/stores/{id}")
    ApiResult<StoreDTO> getStore(@PathVariable("id") Long storeId);

    @GetMapping("/api/v3/internal/products/{id}")
    ApiResult<ProductDTO> getProduct(@PathVariable("id") Long productId);

    @GetMapping("/api/v3/internal/products/skus/{id}")
    ApiResult<ProductSkuDTO> getSku(@PathVariable("id") Long skuId);

    @PostMapping("/api/v3/internal/products/skus/{id}/deduct")
    ApiResult<Boolean> deductSkuStock(@PathVariable("id") Long skuId, @RequestParam("quantity") Integer quantity);

    @PostMapping("/api/v3/internal/products/skus/{id}/restore")
    ApiResult<Boolean> restoreSkuStock(@PathVariable("id") Long skuId, @RequestParam("quantity") Integer quantity);

    @GetMapping("/api/v3/internal/products/stores/by-merchant")
    ApiResult<List<StoreDTO>> storesByMerchant(@RequestParam("merchantUserId") Long merchantUserId);
}
