package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.OrderSaga;

import java.time.Instant;
import java.util.List;

public record SagaView(String orderId, String status, String currentStep, List<String> completedSteps, Instant stepStartedAt,
                       int attempts, String failureReason) {

    static SagaView from(final OrderSaga saga) {
        return new SagaView(saga.getOrderId().value().toString(), saga.getStatus().name(),
                saga.getCurrentStep() == null ? null : saga.getCurrentStep().name(),
                saga.getCompletedSteps().stream().map(Enum::name).toList(), saga.getStepStartedAt(), saga.getAttempts(), saga.getFailureReason());
    }

}
