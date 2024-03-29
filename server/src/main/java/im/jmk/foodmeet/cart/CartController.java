package im.jmk.foodmeet.cart;

import im.jmk.foodmeet.cart.dto.CartItemDto;
import im.jmk.foodmeet.cart.dto.CartItemPatchDto;
import im.jmk.foodmeet.cart.dto.CartItemPostDto;
import im.jmk.foodmeet.common.response.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartFacade cartFacade;

    @PostMapping
    public ResponseEntity<Response<CartItemDto>> postCartItem(@Valid @RequestBody CartItemPostDto postDto) {
        return new ResponseEntity<>(Response.of(cartFacade.addCartItem(postDto)), HttpStatus.CREATED);
    }

    @GetMapping("/{cartItemId}")
    public ResponseEntity<Response<CartItemDto>> getCartItem(@Positive @PathVariable long cartItemId) {
        return new ResponseEntity<>(Response.of(cartFacade.readCartItem(cartItemId)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Response<List<CartItemDto>>> getCart() {
        return new ResponseEntity<>(Response.of(cartFacade.readCart()), HttpStatus.OK);
    }

    @PatchMapping("/{cartItemId}")
    public ResponseEntity<Response<CartItemDto>> patchCartItem(@Positive @PathVariable long cartItemId,
                                                               @Valid @RequestBody CartItemPatchDto patchDto) {
        return new ResponseEntity<>(Response.of(cartFacade.updateCartItem(cartItemId, patchDto)), HttpStatus.OK);
    }

    @DeleteMapping("/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCartItem(@Positive @PathVariable long cartItemId) {
        cartFacade.deleteCartItem(cartItemId);
    }


}
