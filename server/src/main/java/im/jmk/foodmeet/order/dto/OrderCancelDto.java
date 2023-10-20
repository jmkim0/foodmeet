package im.jmk.foodmeet.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelDto {

    @Size(min = 1)
    private List<@Valid OrderProductCancelDto> products;

}
