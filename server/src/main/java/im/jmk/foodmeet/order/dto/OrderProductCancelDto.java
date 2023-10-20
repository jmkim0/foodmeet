package im.jmk.foodmeet.order.dto;

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
