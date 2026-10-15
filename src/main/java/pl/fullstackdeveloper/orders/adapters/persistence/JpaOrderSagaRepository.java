package pl.fullstackdeveloper.orders.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.OrderSagaRepository;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.OrderSaga;
import pl.fullstackdeveloper.orders.domain.SagaStatus;
import pl.fullstackdeveloper.orders.domain.SagaStep;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

interface SpringDataOrderSagaRepository extends JpaRepository<OrderSagaEntity, String> {

    List<OrderSagaEntity> findByStatusIn(List<SagaStatus> statuses);

}

@Adapter
@Transactional(propagation = Propagation.MANDATORY)
class JpaOrderSagaRepository implements OrderSagaRepository {

    private final SpringDataOrderSagaRepository repository;

    JpaOrderSagaRepository(final SpringDataOrderSagaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<OrderSaga> findById(final OrderId orderId) {
        return repository.findById(orderId.value().toString()).map(this::toDomain);
    }

    @Override
    public List<OrderSaga> findActive() {
        return repository.findByStatusIn(List.of(SagaStatus.RUNNING, SagaStatus.COMPENSATING)).stream().map(this::toDomain).toList();
    }

    @Override
    public void save(final OrderSaga saga) {
        var id = saga.getOrderId().value().toString();
        var entity = repository.findById(id).orElseGet(() -> new OrderSagaEntity(id));
        entity.setStatus(saga.getStatus());
        entity.setCurrentStep(saga.getCurrentStep());
        entity.setCompletedSteps(saga.getCompletedSteps().stream().map(Enum::name).collect(Collectors.joining(",")));
        entity.setStepStartedAt(saga.getStepStartedAt());
        entity.setAttempts(saga.getAttempts());
        entity.setFailureReason(saga.getFailureReason());
        repository.save(entity);
    }

    private OrderSaga toDomain(final OrderSagaEntity entity) {
        var completed = entity.getCompletedSteps() == null || entity.getCompletedSteps().isBlank()
                ? List.<SagaStep>of()
                : Arrays.stream(entity.getCompletedSteps().split(",")).map(SagaStep::valueOf).toList();
        return OrderSaga.restore(new OrderId(UUID.fromString(entity.getOrderId())), entity.getStatus(), entity.getCurrentStep(), completed,
                entity.getStepStartedAt(), entity.getAttempts(), entity.getFailureReason());
    }

}
