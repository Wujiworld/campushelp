package com.campushelp.user.controller;

import com.campushelp.common.security.SecurityContextUtils;
import com.campushelp.user.dto.AddressRequest;
import com.campushelp.user.entity.ChAddress;
import com.campushelp.user.service.AddressService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/api/v3/addresses")
    public List<ChAddress> list() {
        return addressService.listMine(SecurityContextUtils.requireUserId());
    }

    @PostMapping("/api/v3/addresses")
    public ChAddress create(@Valid @RequestBody AddressRequest req) {
        return addressService.create(SecurityContextUtils.requireUserId(), req);
    }

    @PutMapping("/api/v3/addresses/{id}")
    public ChAddress update(@PathVariable("id") Long id, @Valid @RequestBody AddressRequest req) {
        return addressService.update(SecurityContextUtils.requireUserId(), id, req);
    }

    @DeleteMapping("/api/v3/addresses/{id}")
    public void delete(@PathVariable("id") Long id) {
        addressService.delete(SecurityContextUtils.requireUserId(), id);
    }

    @PostMapping("/api/v3/addresses/{id}/default")
    public ChAddress setDefault(@PathVariable("id") Long id) {
        return addressService.setDefault(SecurityContextUtils.requireUserId(), id);
    }
}
