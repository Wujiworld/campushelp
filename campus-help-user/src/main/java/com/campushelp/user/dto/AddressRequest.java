package com.campushelp.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AddressRequest {

    @NotNull
    private Long campusId;

    private Long buildingId;

    @NotBlank
    private String contactName;

    @NotBlank
    private String contactPhone;

    @NotBlank
    private String detail;

    private String label;

    /** 是否设为默认地址 */
    private Boolean defaultAddress;
}
