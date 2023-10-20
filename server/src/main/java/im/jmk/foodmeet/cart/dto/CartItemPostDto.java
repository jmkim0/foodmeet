package im.jmk.foodmeet.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CartItemPostDto {

    @NotNull
    @Positive
    private int productId;

    @NotNull
    @Positive
    private int quantity;

}
