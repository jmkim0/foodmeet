package im.jmk.foodmeet.product;

import im.jmk.foodmeet.common.exception.BusinessLogicException;
import im.jmk.foodmeet.common.exception.ExceptionCode;
import im.jmk.foodmeet.product.dto.ProductPatchDto;
import im.jmk.foodmeet.product.dto.ProductPostDto;
import im.jmk.foodmeet.product.entity.Product;
import im.jmk.foodmeet.product.entity.ProductCategory;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@AllArgsConstructor
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(ProductPostDto postDto, String imageUrls, String detailImageUrls) {
        // 엔티티 객체 생성
        Product product = new Product(postDto.getName(), postDto.getPrice(), postDto.getStock());

        // 선택 항목 처리
        Optional.ofNullable(imageUrls).ifPresent(product::setImageUrls);
        Optional.ofNullable(detailImageUrls).ifPresent(product::setDetailImageUrls);
        Optional.ofNullable(postDto.getStatus()).ifPresent(product::setStatus);
        Optional.ofNullable(postDto.getCategory()).ifPresent(product::setCategory);

        // 엔티티 DB에 저장 후 반환
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product readProduct(int productId) {
        // DB에서 상품 조회 및 반환, 없는 경우 예외 발생
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.PRODUCT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Product> readProducts(Pageable pageable) {
        // 전체 조회
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> readProducts(ProductCategory category, Pageable pageable) {
        // 카테고리에 맞게 조회
        return productRepository.findByCategory(category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> readProducts(String q, Pageable pageable) {
        return productRepository.findByNameLike(q, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> readProducts(ProductCategory category, String q, Pageable pageable) {
        return productRepository.findByCategoryAndNameLike(category, q, pageable);
    }

    public Product updateProduct(int productId, ProductPatchDto patchDto, String imageUrls, String detailImageUrls) {
        // DB에서 상품 조회, 없는 경우 예외 발생
        Product product = readProduct(productId);

        // DTO에 입력된 값으로 변경
        Optional<ProductPatchDto> optionalPatchDto = Optional.ofNullable(patchDto);
        optionalPatchDto.map(ProductPatchDto::getName).ifPresent(product::setName);
        optionalPatchDto.map(ProductPatchDto::getPrice).ifPresent(product::setPrice);
        optionalPatchDto.map(ProductPatchDto::getStock).ifPresent(product::setStock);
        optionalPatchDto.map(ProductPatchDto::getStatus).ifPresent(product::setStatus);
        optionalPatchDto.map(ProductPatchDto::getCategory).ifPresent(product::setCategory);

        // 이미지 주소 변경
        product.setImageUrls(imageUrls);
        product.setDetailImageUrls(detailImageUrls);

        return product;
    }

    @Transactional(readOnly = true)
    public List<Product> getVerifiedProducts(Set<Integer> productIds) {
        // 모든 ID에 해당하는 상품을 조회
        List<Product> products = productRepository.findAllById(productIds);

        // 조회된 상품 수가 ID 수보다 적으면 예외 발생
        if (products.size() != productIds.size()) {
            throw new BusinessLogicException(ExceptionCode.PRODUCT_NOT_FOUND);
        }

        return products;
    }

    public void updateProductStockSold(int productId, int delta) {
        // DB에서 상품 조회, 없는 경우 예외 발생
        Product product = readProduct(productId);

        // 수정될 재고량, 판매량 계산
        // delta: 재고량 기준 변화량
        int updatedStock = product.getStock() + delta;
        int updatedSold = product.getSold() - delta;

        // 재고가 부족한 경우 예외 발생
        if (updatedStock < 0) {
            throw new BusinessLogicException(ExceptionCode.PRODUCT_NOT_ENOUGH_STOCK);
        }

        // 재고량 맟 판매량 수정
        product.setStock(updatedStock);
        product.setSold(updatedSold);
    }

}
