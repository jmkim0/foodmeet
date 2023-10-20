package im.jmk.foodmeet.order.entity;

public enum OrderProductStatus {
    WAITING_FOR_PAYMENT,
    PAYMENT_FINISHED,
    PREPARING_FOR_SHIPMENT,
    SHIPPED,
    DELIVERED,
    WAITING_FOR_CANCELLATION,
    CANCELED
}
