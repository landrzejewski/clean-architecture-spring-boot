package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.OrderId;

public interface GetOrderUseCase {

    OrderView handle(OrderId orderId);

}
