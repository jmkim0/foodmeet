package im.jmk.foodmeet.useraddress;

import im.jmk.foodmeet.common.Address;
import im.jmk.foodmeet.common.auditing.entity.DateAuditable;
import im.jmk.foodmeet.useraddress.dto.UserAddressDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(indexes = @Index(name = "idx_user_address_user_id", columnList = "userId"))
public class UserAddress extends DateAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int userId;

    @Embedded
    private Address address;

    public UserAddressDto toDto(Long primaryId) {
        return new UserAddressDto(id, userId, address.getRecipient(), address.getAddress(),
                id.equals(primaryId), getCreatedAt(), getModifiedAt());
    }

}
