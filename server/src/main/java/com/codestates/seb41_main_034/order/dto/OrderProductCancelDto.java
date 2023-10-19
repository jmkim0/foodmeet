package com.codestates.seb41_main_034.order.dto;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderProductCancelDto {

    @Positive
    private int productId;

    @Positive
    private int quantity;

}
