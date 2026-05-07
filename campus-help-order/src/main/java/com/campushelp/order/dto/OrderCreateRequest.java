package com.campushelp.order.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.List;

/**
 * 创建订单请求。
 * <p>
 * V2：{@code userId} 不必传，由服务端从 JWT 注入；若传入则以 Token 中用户为准（防伪造）。
 */
@Data
public class OrderCreateRequest {

    /** 兼容旧客户端；实际以登录用户为准 */
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
