package com.campushelp.life.activity.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AdminTicketTypeCreateRequest {
    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Integer priceCent;

    @NotNull
    @Min(1)
    private Integer stockTotal;

    @NotNull
    @Min(1)
    private Integer perUserLimit;

    @NotNull
    private LocalDateTime saleStartTime;

    @NotNull
    private LocalDateTime saleEndTime;

    /**
     * ON / OFF
     */
    private String status;
}

