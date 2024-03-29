package im.jmk.foodmeet.useraddress;

import im.jmk.foodmeet.common.Address;
import im.jmk.foodmeet.common.exception.BusinessLogicException;
import im.jmk.foodmeet.common.exception.ExceptionCode;
import im.jmk.foodmeet.useraddress.dto.UserAddressPatchDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Transactional
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;

    public UserAddress createUserAddress(int userId, Address address) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        userAddress.setAddress(address);

        return userAddressRepository.save(userAddress);
    }

    @Transactional(readOnly = true)
    public UserAddress readUserAddress(long userAddressId) {
        return userAddressRepository.findById(userAddressId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.USER_ADDRESS_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<UserAddress> readUserAddressList(int userId) {
        return userAddressRepository.findByUserId(userId);
    }

    public UserAddress updateUserAddress(long userAddressId, UserAddressPatchDto patchDto) {
        UserAddress userAddress = readUserAddress(userAddressId);
        Optional.ofNullable(patchDto.getRecipient()).ifPresent(s -> userAddress.getAddress().setRecipient(s));
        Optional.ofNullable(patchDto.getAddress()).ifPresent(s -> userAddress.getAddress().setAddress(s));

        return userAddress;
    }

    public void deleteUserAddress(long userAddressId) {
        UserAddress userAddress = readUserAddress(userAddressId);

        userAddress.setDeleted(true);
    }

}
