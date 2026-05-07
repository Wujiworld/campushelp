package com.campushelp.order.client.dto;

import lombok.Data;

@Data
public class StoreDTO {
    private Long id;
    private Long merchantUserId;
    private Long campusId;
    private Integer status;
}
