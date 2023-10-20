package im.jmk.foodmeet.product;

import im.jmk.foodmeet.common.response.PaginatedData;
import im.jmk.foodmeet.common.response.Response;
import im.jmk.foodmeet.product.dto.ProductDto;
import im.jmk.foodmeet.product.dto.ProductPatchDto;
import im.jmk.foodmeet.product.dto.ProductPostDto;
import im.jmk.foodmeet.product.entity.ProductCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1/product")
public class ProductController {

    private final ProductFacade productFacade;

    @PostMapping
    public ResponseEntity<Response<ProductDto>> postProduct(
            @Valid @RequestPart("data-json") ProductPostDto postDto,
            @RequestPart(name = "images", required = false) List<MultipartFile> images,
            @RequestPart(name = "detail-images", required = false) List<MultipartFile> detailImages
    ) {
        return new ResponseEntity<>(
                Response.of(productFacade.createProduct(postDto, images, detailImages)), HttpStatus.CREATED);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Response<ProductDto>> getProduct(@Positive @PathVariable int productId) {
        return new ResponseEntity<>(Response.of(productFacade.readProduct(productId)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Response<PaginatedData<ProductDto>>> getProducts(
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) @Size(min = 1) String q,
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(Response.of(productFacade.readProducts(category, q, pageable)), HttpStatus.OK);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Response<ProductDto>> patchProduct(
            @Positive @PathVariable int productId,
            @Valid @RequestPart(name = "data-json", required = false) ProductPatchDto patchDto,
            @RequestPart(name = "images", required = false) List<MultipartFile> images,
            @RequestPart(name = "detail-images", required = false) List<MultipartFile> detailImages
    ) {
        return new ResponseEntity<>(
                Response.of(productFacade.updateProduct(productId, patchDto, images, detailImages)), HttpStatus.OK);
    }

}
