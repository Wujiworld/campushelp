package com.campushelp.order.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.List;

/**
 * 创建订单请求（主链路演示：暂未接登录，userId 由调用方显式传入）。
 */
@Data
public class OrderCreateRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String orderType;

    @NotNull
    private Long campusId;

    private Long storeId;
    private Long addressId;

    @NotNull
    @Min(0)
    private Integer deliveryFeeCent;

    @Valid
    @NotNull
    private List<OrderItemRequest> items;

    private String remark;
}
