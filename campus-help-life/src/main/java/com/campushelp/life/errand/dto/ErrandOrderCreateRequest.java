package com.campushelp.life.errand.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ErrandOrderCreateRequest {

    @NotNull
    private Long campusId;

    private Long addressId;

    @NotBlank
    private String errandType;

    private String pickupAddress;
    private String pickupCode;
    private String listText;

    @NotNull
    @Min(1)
    private Integer feeCent;

    private String remark;
}
