package com.campushelp.life.agent.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class AgentItemCreateRequest {

    @NotBlank
    @Size(max = 128)
    private String title;

    @Size(max = 2048)
    private String description;

    @NotNull
    @Min(1)
    private Integer priceCent;
}
