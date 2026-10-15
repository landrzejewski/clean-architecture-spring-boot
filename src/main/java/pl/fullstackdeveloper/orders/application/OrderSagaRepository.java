package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.OrderSaga;

import java.util.List;
import java.util.Optional;

public interface OrderSagaRepository {

    Optional<OrderSaga> findById(OrderId orderId);

    List<OrderSaga> findActive();

    void save(OrderSaga saga);

}
