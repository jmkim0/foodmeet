package com.codestates.seb41_main_034.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CartItemPatchDto {

    @NotNull
    @Positive
    private int quantity;

}
