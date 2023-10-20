package im.jmk.foodmeet.order.dto;

import im.jmk.foodmeet.order.entity.OrderProductStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class OrderProductDto {

    private int productId;

    private String productName;

    private String imageUrl;

    private int price;

    private int quantity;

    private OrderProductStatus status;

    private int createdBy;

    private int modifiedBy;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

}
