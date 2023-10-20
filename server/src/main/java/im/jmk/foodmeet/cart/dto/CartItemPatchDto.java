package im.jmk.foodmeet.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CartItemPatchDto {

    @NotNull
    @Positive
    private int quantity;

}
