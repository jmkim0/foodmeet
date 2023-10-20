package im.jmk.foodmeet.product.dto;

import im.jmk.foodmeet.product.entity.ProductCategory;
import im.jmk.foodmeet.product.entity.ProductStatus;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProductPatchDto {

    @Size(min = 1)
    private String name;

    @PositiveOrZero
    private Integer price;

    @PositiveOrZero
    private Integer stock;

    private ProductStatus status;

    private ProductCategory category;

    private List<Boolean> deleteImage;

    private List<Boolean> deleteDetailImage;

}
