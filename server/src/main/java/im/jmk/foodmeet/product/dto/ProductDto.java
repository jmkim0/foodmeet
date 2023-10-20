package im.jmk.foodmeet.product.dto;

import im.jmk.foodmeet.product.entity.ProductCategory;
import im.jmk.foodmeet.product.entity.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ProductDto {

    private int id;

    private String name;

    private int price;

    private int stock;

    private int sold;

    private int reviewed;

    private ProductStatus status;

    private ProductCategory category;

    private List<String> imageUrls;

    private List<String> detailImageUrls;

    private int createdBy;

    private int modifiedBy;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

}
