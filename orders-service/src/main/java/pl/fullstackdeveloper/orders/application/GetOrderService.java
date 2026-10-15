package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.OrderId;

@Atomic(readOnly = true)
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepository orderRepository;

    public GetOrderService(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderView handle(final OrderId orderId) {
        return orderRepository.findById(orderId).map(OrderView::from).orElseThrow(OrderNotFoundException::new);
    }

}
