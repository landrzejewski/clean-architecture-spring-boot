package pl.fullstackdeveloper.orders.adapters.persistence;

import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.OrderRepository;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Adapter
class InMemoryOrderRepository implements OrderRepository {

    private final Map<OrderId, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(final OrderId id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findByCardReference(final CardReference cardReference) {
        return orders.values().stream().filter(order -> order.getCardReference().equals(cardReference)).toList();
    }

    @Override
    public Order save(final Order order) {
        orders.put(order.getId(), order);
        return order;
    }

}
