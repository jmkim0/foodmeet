package im.jmk.foodmeet.useraddress.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserAddressPatchDto {

    @Size(min = 1)
    private String recipient;

    @Size(min = 1)
    private String address;

    private boolean isPrimary;

}
