package im.jmk.foodmeet.useraddress.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserAddressDto {

    private long id;

    private int userId;

    private String recipient;

    private String address;

    private boolean isPrimary;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

}
