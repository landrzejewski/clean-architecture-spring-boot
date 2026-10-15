package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;

/*
 Choreografia: zamówienia nie wydają poleceń (zapłać, wyślij) - ogłaszają fakt OrderPlaced.
 Dalszy przebieg wynika z reakcji innych modułów na zdarzenia.
 */
@Atomic
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderEvents orderEvents;

    public PlaceOrderService(final OrderRepository orderRepository, final OrderEvents orderEvents) {
        this.orderRepository = orderRepository;
        this.orderEvents = orderEvents;
    }

    @Override
    public OrderId handle(final PlaceOrderCommand command) {
        var order = new Order(new OrderId(), command.description(), command.cardReference(), command.total(), command.address());
        orderRepository.save(order);
        orderEvents.orderPlaced(order);
        return order.getId();
    }

}
