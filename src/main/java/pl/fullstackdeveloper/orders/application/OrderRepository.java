package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.CardReference;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    List<Order> findByCardReference(CardReference cardReference);

    Order save(Order order);

}
