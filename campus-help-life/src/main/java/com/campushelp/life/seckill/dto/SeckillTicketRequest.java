package com.campushelp.life.seckill.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SeckillTicketRequest {
    @NotNull
    private Long campusId;
    @NotNull
    private Long ticketTypeId;
}
