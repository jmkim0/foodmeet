package im.jmk.foodmeet.order.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class OrderDto {

    private long id;

    private List<OrderProductDto> products;

    private String recipient;

    private String address;

    private int createdBy;

    private int modifiedBy;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

}
