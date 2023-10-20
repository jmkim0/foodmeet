package im.jmk.foodmeet.product.dto;

import im.jmk.foodmeet.product.entity.ProductCategory;
import im.jmk.foodmeet.product.entity.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductPostDto {

    @NotBlank
    private String name;

    @PositiveOrZero
    private int price;

    @PositiveOrZero
    private int stock;

    private ProductStatus status;

    private ProductCategory category;

}
