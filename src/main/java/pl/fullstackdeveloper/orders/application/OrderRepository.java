package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    List<Order> findByCardNumber(CardNumber cardNumber);

    Order save(Order order);

}
