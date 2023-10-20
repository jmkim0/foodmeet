package im.jmk.foodmeet.review;

import im.jmk.foodmeet.common.JsonListHelper;
import im.jmk.foodmeet.common.auditing.entity.Auditable;
import im.jmk.foodmeet.product.entity.Product;
import im.jmk.foodmeet.review.dto.ReviewDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "idx_review_product_id_order_id", columnList = "productId, orderId"),
        @Index(name = "idx_review_created_by_created_at", columnList = "createdBy, createdAt")
})
public class Review extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long orderId;

    @Column(nullable = false)
    private int productId;

    public Review(long orderId, int productId, String body) {
        this.orderId = orderId;
        this.productId = productId;
        this.body = body;
    }

    @Column(nullable = false)
    private String body;

    public ReviewDto toDto(JsonListHelper helper, Product product, String createdByName) {
        List<String> productImageUrlList = helper.jsonToList(product.getImageUrls());
        String productImageUrl = productImageUrlList.isEmpty() ? null : productImageUrlList.get(0);

        return new ReviewDto(id, productId, product.getName(), productImageUrl, body,
                getCreatedBy(), createdByName, getModifiedBy(), getCreatedAt(), getModifiedAt());
    }

    public ReviewDto toDto() {

        return new ReviewDto(id, productId, null, null, body,
                getCreatedBy(), null, getModifiedBy(), getCreatedAt(), getModifiedAt());
    }

}
