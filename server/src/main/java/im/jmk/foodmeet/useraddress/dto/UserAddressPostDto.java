package im.jmk.foodmeet.useraddress.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserAddressPostDto {

    @NotBlank
    private String recipient;

    @NotBlank
    private String address;

    private boolean isPrimary;

}
