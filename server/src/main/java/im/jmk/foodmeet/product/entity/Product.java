package im.jmk.foodmeet.product.entity;

import im.jmk.foodmeet.common.JsonListHelper;
import im.jmk.foodmeet.common.auditing.entity.Auditable;
import im.jmk.foodmeet.product.dto.ProductDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_category_name", columnList = "category, name"),
})
public class Product extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private int sold;

    @Column(nullable = false)
    private int reviewed;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category = ProductCategory.NO_CATEGORY;

    @Column(nullable = false)
    private String imageUrls = "[]";

    @Column(nullable = false)
    private String detailImageUrls = "[]";

    public Product(String name, int price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public ProductDto toDto(JsonListHelper helper) {
        return new ProductDto(
                id, name, price, stock, sold, reviewed, status, category,
                helper.jsonToList(imageUrls), helper.jsonToList(detailImageUrls),
                getCreatedBy(), getModifiedBy(), getCreatedAt(), getModifiedAt()
        );
    }

}
