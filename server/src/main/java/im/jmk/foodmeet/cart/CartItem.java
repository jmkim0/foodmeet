package im.jmk.foodmeet.cart;

import im.jmk.foodmeet.cart.dto.CartItemDto;
import im.jmk.foodmeet.common.JsonListHelper;
import im.jmk.foodmeet.common.auditing.entity.DateAuditable;
import im.jmk.foodmeet.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = @Index(name = "idx_cart_item_user_id_product_id", columnList = "userId, productId"))
public class CartItem extends DateAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private int userId;

    @Column(nullable = false, updatable = false)
    private int productId;

    @Column(nullable = false)
    private int quantity;

    public CartItem(int productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public CartItemDto toDto(JsonListHelper helper, Product product) {
        List<String> urlList = helper.jsonToList(product.getImageUrls());
        String imageUrl = urlList.isEmpty() ? null : urlList.get(0);

        return new CartItemDto(id, userId, productId, product.getName(), imageUrl, product.getPrice(), quantity,
                getCreatedAt(), getModifiedAt());
    }

    public CartItemDto toDto() {
        return new CartItemDto(
                id, userId, productId, null, null, null, quantity, getCreatedAt(), getModifiedAt());
    }

}
