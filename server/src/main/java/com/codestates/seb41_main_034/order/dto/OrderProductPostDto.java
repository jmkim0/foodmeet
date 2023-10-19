package com.codestates.seb41_main_034.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderProductPostDto {

    @NotNull
    @Positive
    private int productId;

    @NotNull
    @Positive
    private int price;

    @NotNull
    @Positive
    private int quantity;

}
