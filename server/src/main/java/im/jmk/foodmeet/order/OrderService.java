package im.jmk.foodmeet.order;

import im.jmk.foodmeet.common.Address;
import im.jmk.foodmeet.common.exception.BusinessLogicException;
import im.jmk.foodmeet.common.exception.ExceptionCode;
import im.jmk.foodmeet.order.dto.OrderAddressPatchDto;
import im.jmk.foodmeet.order.dto.OrderCancelDto;
import im.jmk.foodmeet.order.dto.OrderPostDto;
import im.jmk.foodmeet.order.dto.OrderProductCancelDto;
import im.jmk.foodmeet.order.entity.Order;
import im.jmk.foodmeet.order.entity.OrderProduct;
import im.jmk.foodmeet.order.entity.OrderProductStatus;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(OrderPostDto postDto) {
        // Order 생성
        Order order = new Order();

        // OrderProduct 생성
        // DTO에 중복된 상품 ID가 있는 경우 수량을 합쳐서 하나의 OrderProduct 엔티티로 처리
        Map<Integer, OrderProduct> orderProductMap = new HashMap<>();
        postDto.getProducts().forEach(dto -> {
            int productId = dto.getProductId();
            orderProductMap.compute(productId, (k, v) -> {
                if (v == null) {
                    return new OrderProduct(order, productId, dto.getPrice(), dto.getQuantity());
                }
                v.setQuantity(v.getQuantity() + dto.getQuantity());
                return v;
            });
        });
        order.setOrderProducts(new ArrayList<>(orderProductMap.values()));

        // 주소 입력
        Address address = new Address(postDto.getRecipient(), postDto.getAddress());
        order.setAddress(address);

        // DB에 저장 후 반환
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order readOrder(long orderId) {
        // DB에서 주문 조회 후 반환, 없는 경우 예외 발생
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Order> readOrders(int createdBy, LocalDate from, LocalDate to, Pageable pageable) {
        // 페이지네이션 적용된 주문 ID 조회
        Page<Long> orderIdPage = orderRepository.findIdByCreatedByAndDateBetween(createdBy, from, to, pageable);

        // 주문 ID Page를 사용하여 fetch join된 주문 목록 조회
        Map<Long, Order> orders = orderRepository.findAllById(orderIdPage.getContent()).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));

        // 주문 목록 Page 반환
        return orderIdPage.map(orders::get);
    }

    public Order updateOrderAddress(long orderId, OrderAddressPatchDto addressPatchDto) {
        // DB에서 주문 조회, 없는 경우 예외 발생
        Order order = readOrder(orderId);

        // 배송 진행중인 상품이 없고 결제 대기중, 결제 완료 상태인 상품만 있을 때 주소 수정 가능
        order.getOrderProducts()
                .stream()
                .map(OrderProduct::getStatus)
                .distinct()
                .peek(status -> {
                    if (status == OrderProductStatus.PREPARING_FOR_SHIPMENT
                            || status == OrderProductStatus.SHIPPED
                            || status == OrderProductStatus.DELIVERED) {
                        throw new BusinessLogicException(ExceptionCode.ORDER_CANNOT_UPDATE_ADDRESS);
                    }
                })
                .forEach(status -> {
                    if (status != OrderProductStatus.WAITING_FOR_PAYMENT
                            && status != OrderProductStatus.PAYMENT_FINISHED) {
                        throw new BusinessLogicException(ExceptionCode.ORDER_CANNOT_UPDATE_ADDRESS);
                    }
                });

        // DTO에 입력된 값으로 주소 수정
        Address address = order.getAddress();
        address.setRecipient(addressPatchDto.getRecipient());
        address.setAddress(addressPatchDto.getAddress());

        return order;
    }

    public Order updateOrderCancel(long orderId, OrderCancelDto cancelDto) {
        // DB에서 주문 조회, 없는 경우 예외 발생
        Order order = readOrder(orderId);

        // DTO를 Map<productId, quantity>로 만든다. DTO에 중복된 ID가 있는 경우 quantity를 더한다.
        Map<Integer, Integer> cancelMap = cancelDto.getProducts().stream().collect(Collectors.toMap(
                OrderProductCancelDto::getProductId, OrderProductCancelDto::getQuantity, Integer::sum));

        // 취소 가능한 OrderProduct만 분리
        // 결제 대기, 결제 완료, 배송 준비 중일 때에만 취소 가능
        List<OrderProduct> cancelableOrderProducts = order.getOrderProducts().stream().filter(orderProduct -> {
            OrderProductStatus status = orderProduct.getStatus();
            return status == OrderProductStatus.WAITING_FOR_PAYMENT
                    || status == OrderProductStatus.PAYMENT_FINISHED
                    || status == OrderProductStatus.PREPARING_FOR_SHIPMENT;
        }).collect(Collectors.toList());

        // 취소 처리 진행
        cancelMap.forEach((productId, quantity) -> {
            // 상품 ID에 해당하는 OrderProduct만 분리
            List<OrderProduct> orderProducts = cancelableOrderProducts.stream()
                    .filter(orderProduct -> orderProduct.getProductId() == productId).collect(Collectors.toList());

            // 취소 가능한 수량보다 취소 요청 수량이 큰 경우 예외 발생
            int cancelableQuantity = orderProducts.stream().mapToInt(OrderProduct::getQuantity).sum();
            if (cancelableQuantity < quantity) {
                throw new BusinessLogicException(ExceptionCode.ORDER_CANNOT_CANCEL);
            }

            // OrderProduct를 하나씩 취소 처리
            for (OrderProduct orderProduct : orderProducts) {
                OrderProductStatus status = orderProduct.getStatus();

                // 결제 대기 중, 결제 완료인 경우 바로 취소 완료 처리
                if (status == OrderProductStatus.WAITING_FOR_PAYMENT
                        || status == OrderProductStatus.PAYMENT_FINISHED) {
                    // 취소 요청 수량보다 OrderProduct의 구매 수량이 적거나 같으면 전체를 취소한다.
                    if (orderProduct.getQuantity() <= quantity) {
                        orderProduct.setStatus(OrderProductStatus.CANCELED);
                        quantity -= orderProduct.getQuantity();
                        // 취소 요청 수량보다 OrderProduct의 구매 수량이 많으면 취소 요청 수량만큼 구매 수량을 줄인다.
                        // 취소된 수량 만큼 새로운 OrderProduct를 만들어서 기록해준다.
                    } else {
                        orderProduct.setQuantity(orderProduct.getQuantity() - quantity);
                        OrderProduct canceledOrderProduct =
                                new OrderProduct(order, productId, orderProduct.getPrice(), quantity);
                        canceledOrderProduct.setStatus(OrderProductStatus.CANCELED);
                        order.getOrderProducts().add(canceledOrderProduct);
                        quantity = 0;
                    }
                } else { // 배송 준비 중인 경우 취소 대기 처리
                    // 취소 요청 수량보다 OrderProduct의 구매 수량이 적거나 같으면 전체를 취소 대기 처리한다.
                    if (orderProduct.getQuantity() <= quantity) {
                        orderProduct.setStatus(OrderProductStatus.WAITING_FOR_CANCELLATION);
                        quantity -= orderProduct.getQuantity();
                        // 취소 요청 수량보다 OrderProduct의 구매 수량이 많으면 취소 요청 수량만큼 구매 수량을 줄인다.
                        // 취소 대기 수량 만큼 새로운 OrderProduct를 만들어서 기록해준다.
                    } else {
                        orderProduct.setQuantity(orderProduct.getQuantity() - quantity);
                        OrderProduct canceledOrderProduct =
                                new OrderProduct(order, productId, orderProduct.getPrice(), quantity);
                        canceledOrderProduct.setStatus(OrderProductStatus.WAITING_FOR_CANCELLATION);
                        order.getOrderProducts().add(canceledOrderProduct);
                        quantity = 0;
                    }
                }

                // 취소 요청 수량이 더 이상 없으면 종료한다.
                if (quantity == 0) {
                    break;
                }
            }
        });

        return order;
    }

    public Order updateOrderPay(long orderId) {
        Order order = readOrder(orderId);

        List<OrderProduct> orderProducts = order.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getStatus() == OrderProductStatus.WAITING_FOR_PAYMENT)
                .collect(Collectors.toList());

        if (orderProducts.isEmpty()) {
            throw new BusinessLogicException(ExceptionCode.ORDER_NO_PRODUCTS_TO_PAY);
        }

        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.setStatus(OrderProductStatus.PAYMENT_FINISHED);
        }

        return order;
    }

    public Order updateOrderPrepare(long orderId) {
        // DB에서 주문 조회, 없는 경우 예외 발생
        Order order = readOrder(orderId);

        // 결제 완료된 OrderProduct List 생성
        List<OrderProduct> orderProducts = order.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getStatus() == OrderProductStatus.PAYMENT_FINISHED)
                .collect(Collectors.toList());

        // 배송 준비할 상품이 없는 경우 예외 발생
        if (orderProducts.isEmpty()) {
            throw new BusinessLogicException(ExceptionCode.ORDER_NO_PRODUCTS_TO_PREPARE);
        }

        // 결제 완료된 주문에 대해서 배송 준비 중으로 변경
        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.setStatus(OrderProductStatus.PREPARING_FOR_SHIPMENT);
        }

        return order;
    }

    public Order updateOrderShip(long orderId) {
        // DB에서 주문 조회, 없는 경우 예외 발생
        Order order = readOrder(orderId);

        // 배송 준비, 취소 대기 상태인 OrderProduct List 생성
        List<OrderProduct> orderProducts = order.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getStatus() == OrderProductStatus.PREPARING_FOR_SHIPMENT
                        || orderProduct.getStatus() == OrderProductStatus.WAITING_FOR_CANCELLATION)
                .collect(Collectors.toList());

        // 배송할 상품이 없는 경우 예외 발생
        if (orderProducts.isEmpty()) {
            throw new BusinessLogicException(ExceptionCode.ORDER_NO_PRODUCTS_TO_SHIP);
        }

        // 상품 ID마다 하나의 OrderProduct로 수량을 합치고 배송 중으로 변경
        Map<Integer, OrderProduct> orderProductMap = new HashMap<>();
        orderProducts.forEach(orderProduct -> {
            int productId = orderProduct.getProductId();
            orderProductMap.compute(productId, (k, v) -> {
                if (v == null) {
                    orderProduct.setStatus(OrderProductStatus.SHIPPED);
                    return orderProduct;
                }
                v.setQuantity(v.getQuantity() + orderProduct.getQuantity());
                orderProduct.setDeleted(true);
                return v;
            });
        });

        // 삭제된 OrderProduct를 정리
        order.setOrderProducts(order.getOrderProducts().stream()
                .filter(orderProduct -> !orderProduct.isDeleted()).collect(Collectors.toList()));

        return order;
    }

    public Order updateOrderConfirmCancellation(long orderId) {
        // DB에서 주문 조회, 없는 경우 예외 발생
        Order order = readOrder(orderId);

        // 취소 대기 중인 OrderProduct List 생성
        List<OrderProduct> orderProducts = order.getOrderProducts().stream()
                .filter(orderProduct -> orderProduct.getStatus() == OrderProductStatus.WAITING_FOR_CANCELLATION)
                .collect(Collectors.toList());

        // 취소 완료할 상품이 없는 경우 예외 발생
        if (orderProducts.isEmpty()) {
            throw new BusinessLogicException(ExceptionCode.ORDER_NO_PRODUCTS_TO_CONFIRM_CANCELLATION);
        }

        // 취소 대기 중인 상품을 취소 완료로 변경
        for (OrderProduct orderProduct : orderProducts) {
            orderProduct.setStatus(OrderProductStatus.CANCELED);
        }

        return order;
    }

}
