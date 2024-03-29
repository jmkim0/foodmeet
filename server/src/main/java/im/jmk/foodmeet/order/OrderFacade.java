package im.jmk.foodmeet.order;

import im.jmk.foodmeet.common.JsonListHelper;
import im.jmk.foodmeet.common.exception.BusinessLogicException;
import im.jmk.foodmeet.common.exception.ExceptionCode;
import im.jmk.foodmeet.common.response.PaginatedData;
import im.jmk.foodmeet.order.dto.*;
import im.jmk.foodmeet.order.entity.Order;
import im.jmk.foodmeet.order.entity.OrderProduct;
import im.jmk.foodmeet.order.entity.OrderProductStatus;
import im.jmk.foodmeet.product.ProductService;
import im.jmk.foodmeet.product.entity.Product;
import im.jmk.foodmeet.product.entity.ProductStatus;
import im.jmk.foodmeet.user.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class OrderFacade {

    private final OrderService orderService;

    private final ProductService productService;

    private final JsonListHelper helper;

    @Transactional
    public OrderDto createOrder(OrderPostDto postDto) {
        // 입력 받은 상품 ID가 유효하고 주문 가능한지 확인 및 상품 정보 조회
        Set<Integer> productIds = postDto.getProducts().stream()
                .map(OrderProductPostDto::getProductId).collect(Collectors.toSet());
        Map<Integer, Product> productMap = productService.getVerifiedProducts(productIds)
                .stream()
                .peek(product -> {
                    if (product.getStatus() == ProductStatus.UNAVAILABLE) {
                        throw new BusinessLogicException(ExceptionCode.ORDER_UNAVAILABLE_PRODUCT);
                    }
                })
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 상품 원래 가격과 주문 가격 검증
        postDto.getProducts().forEach(dto -> {
            if (dto.getPrice() != productMap.get(dto.getProductId()).getPrice()) {
                throw new BusinessLogicException(ExceptionCode.ORDER_MISMATCHED_PRICE);
            }
        });

        // 주문 저장
        Order order = orderService.createOrder(postDto);

        // 주문한 만큼 재고 감소
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            productService.updateProductStockSold(orderProduct.getProductId(), -orderProduct.getQuantity());
        }

        // DTO로 매핑 후 반환
        return order.toDto(helper, productMap);
    }

    public OrderDto readOrder(long orderId) {
        // 주문 조회
        Order order = orderService.readOrder(orderId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.getRoles().contains("ADMIN") && user.getId() != order.getCreatedBy()) {
            throw new BusinessLogicException(ExceptionCode.AUTH_FORBIDDEN);
        }

        // 주문한 상품 정보 조회
        Set<Integer> productIds = order.getOrderProducts().stream()
                .map(OrderProduct::getProductId).collect(Collectors.toSet());
        Map<Integer, Product> productDtoMap = productService.getVerifiedProducts(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // DTO로 변환 후 반환
        return order.toDto(helper, productDtoMap);
    }

    public PaginatedData<OrderDto> readOrders(LocalDate from, LocalDate to, Pageable pageable) {
        int createdBy = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

        // from, to 기본값 설정
        from = Optional.ofNullable(from).orElse(LocalDate.of(2023, 1, 1));
        to = Optional.ofNullable(to).orElse(LocalDate.now());

        // 주문 목록 Page 조회
        Page<Order> orderPage = orderService.readOrders(createdBy, from, to, pageable);

        // 주문 목록에 있는 상품을 한 번에 조회
        Set<Integer> productIds = orderPage.get().flatMap(order -> order.getOrderProducts().stream())
                .map(OrderProduct::getProductId).collect(Collectors.toSet());
        Map<Integer, Product> productMap = productService.getVerifiedProducts(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 페이지네이션 DTO로 변환 후 반환
        return PaginatedData.of(orderPage.map(order -> order.toDto(helper, productMap)));
    }

    public OrderDto updateOrderAddress(long orderId, OrderAddressPatchDto addressPatchDto) {
        // 주문 조회
        Order order = orderService.readOrder(orderId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.getRoles().contains("ADMIN") && user.getId() != order.getCreatedBy()) {
            throw new BusinessLogicException(ExceptionCode.AUTH_FORBIDDEN);
        }

        // 주문 주소 수정
        Order updatedOrder = orderService.updateOrderAddress(orderId, addressPatchDto);

        // DTO로 변환 후 반환
        return updatedOrder.toDto();
    }

    @Transactional
    public OrderDto updateOrderCancel(long orderId, OrderCancelDto cancelDto) {
        // 주문 조회
        Order order = orderService.readOrder(orderId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.getRoles().contains("ADMIN") && user.getId() != order.getCreatedBy()) {
            throw new BusinessLogicException(ExceptionCode.AUTH_FORBIDDEN);
        }

        // 기존 취소된 수량 집계
        Map<Integer, Integer> productIdDeltaMap = new HashMap<>();
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            if (orderProduct.getStatus() == OrderProductStatus.CANCELED) {
                productIdDeltaMap.merge(orderProduct.getProductId(), -orderProduct.getQuantity(), Integer::sum);
            }
        }

        // 주문 취소
        Order updatedOrder = orderService.updateOrderCancel(orderId, cancelDto);

        // 취소된 수량 집계
        for (OrderProduct orderProduct : updatedOrder.getOrderProducts()) {
            if (orderProduct.getStatus() == OrderProductStatus.CANCELED) {
                productIdDeltaMap.merge(orderProduct.getProductId(), orderProduct.getQuantity(), Integer::sum);
            }
        }

        // 취소 완료된 만큼 재고 증가
        productIdDeltaMap.forEach(productService::updateProductStockSold);

        // 주문한 상품 정보 조회
        Set<Integer> productIds = updatedOrder.getOrderProducts().stream()
                .map(OrderProduct::getProductId).collect(Collectors.toSet());
        Map<Integer, Product> productMap = productService.getVerifiedProducts(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 응답 DTO로 매핑 후 반환
        return updatedOrder.toDto(helper, productMap);
    }

    public OrderDto updateOrderPay(long orderId) {
        // 주문 조회
        Order order = orderService.readOrder(orderId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.getRoles().contains("ADMIN") && user.getId() != order.getCreatedBy()) {
            throw new BusinessLogicException(ExceptionCode.AUTH_FORBIDDEN);
        }

        // TODO: 결제 정보 확인 필요
        Order updatedOrder = orderService.updateOrderPay(orderId);


        return updatedOrder.toDto();
    }

    public OrderDto updateOrderPrepare(long orderId) {
        // 주문 배송 준비 처리
        Order updatedOrder = orderService.updateOrderPrepare(orderId);

        // DTO에 매핑 후 반환
        return updatedOrder.toDto();
    }

    public OrderDto updateOrderShip(long orderId) {
        // 주문 배송 처리
        Order order = orderService.updateOrderShip(orderId);

        // DTO에 매핑 후 반환
        return order.toDto();
    }

    @Transactional
    public OrderDto updateOrderConfirmCancellation(long orderId) {
        // 주문 조회
        Order order = orderService.readOrder(orderId);

        // 기존 취소된 수량 집계
        Map<Integer, Integer> productIdDeltaMap = order.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getStatus() == OrderProductStatus.CANCELED)
                .collect(Collectors.toMap(OrderProduct::getProductId, OrderProduct::getQuantity, Integer::sum));

        // 주문 취소
        Order updatedOrder = orderService.updateOrderConfirmCancellation(orderId);

        // 취소된 수량 집계
        for (OrderProduct orderProduct : updatedOrder.getOrderProducts()) {
            if (orderProduct.getStatus() == OrderProductStatus.CANCELED) {
                productIdDeltaMap.merge(orderProduct.getProductId(), -orderProduct.getQuantity(), Integer::sum);
            }
        }

        // 취소 완료된 만큼 재고 증가
        productIdDeltaMap.forEach(productService::updateProductStockSold);

        // DTO에 매핑 후 반환
        return updatedOrder.toDto();
    }

}
