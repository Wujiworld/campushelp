package com.campushelp.life.merchant.controller;

import com.campushelp.common.security.RequireRole;
import com.campushelp.common.security.RoleEnum;
import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.life.client.dto.ProductSkuDto;
import com.campushelp.life.merchant.dto.MerchantSkuCreateRequest;
import com.campushelp.life.merchant.dto.MerchantSkuUpdateRequest;
import com.campushelp.life.merchant.service.MerchantSkuService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
public class MerchantSkuController {

    private final MerchantSkuService merchantSkuService;

    public MerchantSkuController(MerchantSkuService merchantSkuService) {
        this.merchantSkuService = merchantSkuService;
    }

    @RequireRole({RoleEnum.MERCHANT, RoleEnum.ADMIN})
    @PostMapping("/api/v3/merchant/skus")
    public ProductSkuDto create(@Valid @RequestBody MerchantSkuCreateRequest req) {
        return merchantSkuService.create(SecurityContextUtils.requireUserId(), req);
    }

    @RequireRole({RoleEnum.MERCHANT, RoleEnum.ADMIN})
    @PutMapping("/api/v3/merchant/skus/{id}")
    public ProductSkuDto update(@PathVariable("id") Long id, @Valid @RequestBody MerchantSkuUpdateRequest req) {
        return merchantSkuService.update(SecurityContextUtils.requireUserId(), id, req);
    }

    @RequireRole({RoleEnum.MERCHANT, RoleEnum.ADMIN})
    @PostMapping("/api/v3/merchant/skus/{id}/status")
    public ProductSkuDto status(@PathVariable("id") Long id, @RequestParam int status) {
        return merchantSkuService.setStatus(SecurityContextUtils.requireUserId(), id, status);
    }
}
