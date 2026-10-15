package pl.fullstackdeveloper.orders.application;

public final class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException() {
        super("Order not found");
    }

}
