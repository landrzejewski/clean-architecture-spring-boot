package pl.fullstackdeveloper.orders.adapters.persistence;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.OrderRepository;
import pl.fullstackdeveloper.orders.domain.*;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Zamówienia trafiły do bazy: stan modułu i wychodzące zdarzenia muszą być zapisywane w jednej transakcji
@Adapter
@Transactional(propagation = Propagation.MANDATORY)
class JpaOrderRepositoryAdapter implements OrderRepository {

    private final SpringDataOrderRepository repository;

    JpaOrderRepositoryAdapter(final SpringDataOrderRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Order> findById(final OrderId id) {
        return repository.findById(id.value().toString()).map(this::toDomain);
    }

    @Override
    public List<Order> findByCardReference(final CardReference cardReference) {
        return repository.findByCardReference(cardReference.value()).stream().map(this::toDomain).toList();
    }

    @Override
    public Order save(final Order order) {
        var id = order.getId().value().toString();
        var entity = repository.findById(id).orElseGet(() -> new OrderEntity(id));
        entity.setDescription(order.getDescription());
        entity.setCardReference(order.getCardReference().value());
        entity.setAmount(order.getTotal().amount());
        entity.setCurrencyCode(order.getTotal().currency().getCurrencyCode());
        entity.setStatus(order.getStatus());
        entity.setPaymentId(order.getPaymentId());
        entity.setRejectionReason(order.getRejectionReason());
        return toDomain(repository.save(entity));
    }

    private Order toDomain(final OrderEntity entity) {
        return Order.restore(new OrderId(UUID.fromString(entity.getId())), entity.getDescription(),
                new CardReference(entity.getCardReference()),
                new Price(entity.getAmount(), Currency.getInstance(entity.getCurrencyCode())),
                entity.getStatus(), entity.getPaymentId(), entity.getRejectionReason());
    }

}
