package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.OrderId;

public interface GetOrderSagaUseCase {

    SagaView handle(OrderId orderId);

}
