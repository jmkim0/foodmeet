package im.jmk.foodmeet.user.entity;

import im.jmk.foodmeet.common.Address;
import im.jmk.foodmeet.common.auditing.entity.DateAuditable;
import im.jmk.foodmeet.user.dto.UserDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends DateAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, updatable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    @Column(nullable = false)
    private long primaryAddressId;

    public UserDto toDto(Address address) {
        return new UserDto(id, username, displayName, roles, address.getAddress(), getCreatedAt(), getModifiedAt());
    }

    public UserDto toDto() {
        return new UserDto(id, username, displayName, roles, null, getCreatedAt(), getModifiedAt());
    }

    public String getMaskedName() {
        if (roles.contains("ADMIN")) {
            return displayName;
        }

        if (displayName.length() <= 1) {
            return "*";
        }

        if (displayName.length() == 2) {
            return displayName.charAt(0) + "*";
        }

        return displayName.charAt(0) + "*".repeat(displayName.length() - 2) +
                displayName.charAt(displayName.length() - 1);
    }
}
