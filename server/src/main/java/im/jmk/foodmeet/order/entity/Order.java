package im.jmk.foodmeet.order.entity;

import im.jmk.foodmeet.common.Address;
import im.jmk.foodmeet.common.JsonListHelper;
import im.jmk.foodmeet.common.auditing.entity.Auditable;
import im.jmk.foodmeet.order.dto.OrderDto;
import im.jmk.foodmeet.order.dto.OrderProductDto;
import im.jmk.foodmeet.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@Table(name = "orders", indexes = @Index(name = "idx_orders_created_by_created_at", columnList = "createdBy, createdAt"))
public class Order extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER, mappedBy = "order")
    private List<OrderProduct> orderProducts;

    @Embedded
    private Address address;

    public OrderDto toDto(JsonListHelper helper, Map<Integer, Product> productMap) {
        List<OrderProductDto> orderProductDtos = orderProducts.stream()
                .map(orderProduct -> orderProduct.toDto(helper, productMap.get(orderProduct.getProductId())))
                .collect(Collectors.toList());

        return new OrderDto(id, orderProductDtos, address.getRecipient(), address.getAddress(),
                getCreatedBy(), getModifiedBy(), getCreatedAt(), getModifiedAt());
    }

    public OrderDto toDto() {
        List<OrderProductDto> orderProductDtos =
                orderProducts.stream().map(OrderProduct::toDto).collect(Collectors.toList());

        return new OrderDto(id, orderProductDtos, address.getRecipient(), address.getAddress(),
                getCreatedBy(), getModifiedBy(), getCreatedAt(), getModifiedAt());
    }

}
