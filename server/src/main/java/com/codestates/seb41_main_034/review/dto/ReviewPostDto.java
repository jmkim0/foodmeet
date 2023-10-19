package com.codestates.seb41_main_034.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class ReviewPostDto {

    @NotNull
    @Positive
    private long orderId;

    @NotNull
    @Positive
    private int productId;

    @NotBlank
    private String body;

}
