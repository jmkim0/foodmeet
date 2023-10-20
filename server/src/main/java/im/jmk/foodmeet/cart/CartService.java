package im.jmk.foodmeet.cart;

import im.jmk.foodmeet.cart.dto.CartItemPostDto;
import im.jmk.foodmeet.common.exception.BusinessLogicException;
import im.jmk.foodmeet.common.exception.ExceptionCode;
import im.jmk.foodmeet.user.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Transactional
public class CartService {

    private final CartItemRepository cartItemRepository;

    public CartItem addCartItem(CartItemPostDto postDto) {
        int userId = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        int productId = postDto.getProductId();

        Optional<CartItem> optionalCartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

        CartItem cartItem;
        if (optionalCartItem.isPresent()) {
            cartItem = optionalCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + postDto.getQuantity());

            return cartItem;
        }
        cartItem = new CartItem(postDto.getProductId(), postDto.getQuantity());

        return cartItemRepository.save(cartItem);
    }

    @Transactional(readOnly = true)
    public CartItem readCartItem(long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.CART_ITEM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<CartItem> readCart(int userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public CartItem updateCartItem(long cartItemId, int quantity) {
        CartItem cartItem = readCartItem(cartItemId);
        cartItem.setQuantity(quantity);

        return cartItem;
    }

    public void deleteCartItem(long cartItemId) {
        CartItem cartItem = readCartItem(cartItemId);
        cartItem.setDeleted(true);
    }

}
