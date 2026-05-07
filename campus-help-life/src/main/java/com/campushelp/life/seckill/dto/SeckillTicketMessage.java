package com.campushelp.life.seckill.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillTicketMessage implements Serializable {
    private long userId;
    private long campusId;
    private long ticketTypeId;
    private String idempotencyKey;
}
