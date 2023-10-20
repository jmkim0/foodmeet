package im.jmk.foodmeet.cart;

import im.jmk.foodmeet.cart.dto.CartItemDto;
import im.jmk.foodmeet.cart.dto.CartItemPatchDto;
import im.jmk.foodmeet.cart.dto.CartItemPostDto;
import im.jmk.foodmeet.common.JsonListHelper;
import im.jmk.foodmeet.common.exception.BusinessLogicException;
import im.jmk.foodmeet.common.exception.ExceptionCode;
import im.jmk.foodmeet.product.ProductService;
import im.jmk.foodmeet.product.entity.Product;
import im.jmk.foodmeet.user.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CartFacade {

    private final CartService cartService;

    private final ProductService productService;

    private final JsonListHelper helper;

    public CartItemDto addCartItem(CartItemPostDto postDto) {
        Product product = productService.readProduct(postDto.getProductId());

        CartItem cartItem = cartService.addCartItem(postDto);

        return cartItem.toDto(helper, product);
    }

    public CartItemDto readCartItem(long cartItemId) {
        CartItem cartItem = cartService.readCartItem(cartItemId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.getRoles().contains("ADMIN") && user.getId() != cartItem.getUserId()) {
            throw new BusinessLogicException(ExceptionCode.AUTH_FORBIDDEN);
        }

        Product product = productService.readProduct(cartItem.getProductId());

        return cartItem.toDto(helper, product);
    }

    public List<CartItemDto> readCart() {
        int userId = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

        List<CartItem> cartItems = cartService.readCart(userId);

        Set<Integer> productIds = cartItems.stream().map(CartItem::getProductId).collect(Collectors.toSet());

        Map<Integer, Product> productMap = productService.getVerifiedProducts(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        return cartItems.stream().map(cartItem ->
                cartItem.toDto(helper, productMap.get(cartItem.getProductId()))).collect(Collectors.toList());
    }

    public CartItemDto updateCartItem(long cartItemId, CartItemPatchDto patchDto) {
        CartItem cartItem = cartService.readCartItem(cartItemId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.getRoles().contains("ADMIN") && user.getId() != cartItem.getUserId()) {
            throw new BusinessLogicException(ExceptionCode.AUTH_FORBIDDEN);
        }

        return cartService.updateCartItem(cartItemId, patchDto.getQuantity()).toDto();
    }

    public void deleteCartItem(long cartItemId) {
        CartItem cartItem = cartService.readCartItem(cartItemId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.getRoles().contains("ADMIN") && user.getId() != cartItem.getUserId()) {
            throw new BusinessLogicException(ExceptionCode.AUTH_FORBIDDEN);
        }

        cartService.deleteCartItem(cartItemId);
    }
}
