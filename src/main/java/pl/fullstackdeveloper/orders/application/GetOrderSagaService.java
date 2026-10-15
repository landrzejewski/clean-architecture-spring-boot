package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.OrderId;

@Atomic(readOnly = true)
public class GetOrderSagaService implements GetOrderSagaUseCase {

    private final OrderSagaRepository sagaRepository;

    public GetOrderSagaService(final OrderSagaRepository sagaRepository) {
        this.sagaRepository = sagaRepository;
    }

    @Override
    public SagaView handle(final OrderId orderId) {
        return sagaRepository.findById(orderId).map(SagaView::from).orElseThrow(OrderNotFoundException::new);
    }

}
