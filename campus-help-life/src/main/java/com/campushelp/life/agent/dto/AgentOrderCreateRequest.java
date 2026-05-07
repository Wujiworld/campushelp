package com.campushelp.life.agent.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class AgentOrderCreateRequest {
    @NotNull
    private Long campusId;

    @NotNull
    private Long agentItemId;

    @NotNull
    @Min(1)
    private Long addressId;

    private String remark;
}

