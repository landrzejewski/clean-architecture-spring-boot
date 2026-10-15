package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;

/*
 Use case zależy wyłącznie od własnego portu Payments. W wariancie asynchronicznym zapis zamówienia
 i publikacja PaymentRequested trafiają do bazy w JEDNEJ transakcji (Event Publication Registry) -
 nie ma już okna, w którym zamówienie istnieje, a żądanie płatności zginęło.
 */
@Atomic
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final Payments payments;

    public PlaceOrderService(final OrderRepository orderRepository, final Payments payments) {
        this.orderRepository = orderRepository;
        this.payments = payments;
    }

    @Override
    public OrderId handle(final PlaceOrderCommand command) {
        var order = new Order(new OrderId(), command.description(), command.cardReference(), command.total());
        orderRepository.save(order);
        payments.requestPayment(order.getId(), command.cardReference(), command.total());
        return order.getId();
    }

}
