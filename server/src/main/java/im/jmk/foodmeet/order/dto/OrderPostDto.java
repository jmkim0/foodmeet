package im.jmk.foodmeet.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OrderPostDto {

    @NotNull
    @Size(min = 1)
    private List<@Valid OrderProductPostDto> products;

    @NotBlank
    private String recipient;

    @NotBlank
    private String address;

}
