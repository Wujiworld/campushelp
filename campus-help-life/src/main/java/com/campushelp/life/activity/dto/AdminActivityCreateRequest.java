package com.campushelp.life.activity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class AdminActivityCreateRequest {
    @NotNull
    private Long campusId;

    @NotBlank
    private String title;

    private String description;
    private String place;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    /**
     * DRAFT / PUBLISHED / OFFLINE / ENDED
     */
    private String status;
}

