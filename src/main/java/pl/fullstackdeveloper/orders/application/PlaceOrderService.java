package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;

// Złożenie zamówienia = zapis zamówienia + start sagi (w jednej transakcji)
@Atomic
public class PlaceOrderService implements PlaceOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderSagaOrchestrator sagaOrchestrator;

    public PlaceOrderService(final OrderRepository orderRepository, final OrderSagaOrchestrator sagaOrchestrator) {
        this.orderRepository = orderRepository;
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @Override
    public OrderId handle(final PlaceOrderCommand command) {
        var order = new Order(new OrderId(), command.description(), command.cardReference(), command.total(), command.address());
        orderRepository.save(order);
        sagaOrchestrator.start(order);
        return order.getId();
    }

}
