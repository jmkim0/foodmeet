package im.jmk.foodmeet.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OrderAddressPatchDto {

    @NotBlank
    private String recipient;

    @NotBlank
    private String address;

}
