package com.campushelp.life.secondhand.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SecondhandItemCreateRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    @Min(1)
    private Integer priceCent;

    private Boolean negotiable = true;

    private List<String> imageUrls;
}
