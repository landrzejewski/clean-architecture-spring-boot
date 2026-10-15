package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.OrderStatus;

import java.util.function.Consumer;
import java.util.logging.Logger;

/*
 Reakcje zamówień na fakty z innych modułów. Każda reakcja jest idempotentna: jeśli zamówienie
 nie jest już w oczekiwanym stanie (zdarzenie zdublowane lub spóźnione), nic nie robimy.
 Uwaga: nie ma tu wiedzy o całym procesie - jest rozproszona po modułach.
 */
@Atomic
public class OrderProgressService {

    private static final Logger LOGGER = Logger.getLogger(OrderProgressService.class.getName());

    private final OrderRepository orderRepository;

    public OrderProgressService(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void paid(final OrderId orderId, final String paymentId) {
        update(orderId, OrderStatus.AWAITING_PAYMENT, order -> order.markPaid(paymentId));
    }

    public void paymentFailed(final OrderId orderId, final String reason) {
        update(orderId, OrderStatus.AWAITING_PAYMENT, order -> order.reject(reason));
    }

    public void shipped(final OrderId orderId, final String shipmentId) {
        update(orderId, OrderStatus.PAID, order -> order.confirm(shipmentId));
    }

    public void refunded(final OrderId orderId, final String reason) {
        update(orderId, OrderStatus.PAID, order -> order.cancel(reason));
    }

    private void update(final OrderId orderId, final OrderStatus expected, final Consumer<Order> change) {
        var order = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
        if (order.getStatus() != expected) {
            LOGGER.info("Order %s is %s, ignoring event expecting %s".formatted(orderId.value(), order.getStatus(), expected));
            return;
        }
        change.accept(order);
        orderRepository.save(order);
    }

}
