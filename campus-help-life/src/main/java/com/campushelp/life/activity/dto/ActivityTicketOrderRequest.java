package com.campushelp.life.activity.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ActivityTicketOrderRequest {

    @NotNull
    private Long campusId;

    @NotNull
    private Long ticketTypeId;
}
