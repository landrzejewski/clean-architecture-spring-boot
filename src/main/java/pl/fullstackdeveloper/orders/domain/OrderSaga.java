package pl.fullstackdeveloper.orders.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/*
 Trwały stan sagi składania zamówienia (process manager / orchestrator state).

 Przebieg:      CHARGE_PAYMENT -> SCHEDULE_SHIPMENT -> COMPLETED
 Kompensacja:   ukończone kroki cofamy w ODWROTNEJ kolejności (stos), np. SCHEDULE_SHIPMENT odrzucone
                -> REFUND_PAYMENT -> COMPENSATED

 Stan jest zapisywany po każdym kroku, więc po restarcie aplikacji saga wie, na czym skończyła.
 Odpowiedzi, na które saga aktualnie nie czeka (duplikaty, spóźnione), są ignorowane - awaits(step).
 */
public final class OrderSaga {

    private final OrderId orderId;
    private final Deque<SagaStep> completedSteps = new ArrayDeque<>();
    private SagaStatus status;
    private SagaStep currentStep;
    private Instant stepStartedAt;
    private int attempts;
    private String failureReason;

    private OrderSaga(final OrderId orderId) {
        this.orderId = orderId;
    }

    public static OrderSaga start(final OrderId orderId, final Instant now) {
        var saga = new OrderSaga(orderId);
        saga.status = SagaStatus.RUNNING;
        saga.beginStep(SagaStep.CHARGE_PAYMENT, now);
        return saga;
    }

    public static OrderSaga restore(final OrderId orderId, final SagaStatus status, final SagaStep currentStep, final List<SagaStep> completedSteps,
                                    final Instant stepStartedAt, final int attempts, final String failureReason) {
        var saga = new OrderSaga(orderId);
        saga.status = status;
        saga.currentStep = currentStep;
        completedSteps.forEach(saga.completedSteps::push);
        saga.stepStartedAt = stepStartedAt;
        saga.attempts = attempts;
        saga.failureReason = failureReason;
        return saga;
    }

    public boolean awaits(final SagaStep step) {
        return status.isActive() && currentStep == step;
    }

    public void stepSucceeded(final SagaStep step, final Instant now) {
        requireAwaiting(step);
        if (status == SagaStatus.COMPENSATING) {
            compensateNext(now);
            return;
        }
        completedSteps.push(step);
        switch (step) {
            case CHARGE_PAYMENT -> beginStep(SagaStep.SCHEDULE_SHIPMENT, now);
            case SCHEDULE_SHIPMENT -> finish(SagaStatus.COMPLETED);
            case REFUND_PAYMENT -> throw new IllegalStateException("Compensation step outside of compensation");
        }
    }

    public void stepFailed(final SagaStep step, final String reason, final Instant now) {
        requireAwaiting(step);
        if (status == SagaStatus.COMPENSATING) {
            // kompensacja się nie udała - potrzebna interwencja człowieka (alert, kolejka błędów)
            failureReason = failureReason + "; compensation failed: " + reason;
            finish(SagaStatus.FAILED);
            return;
        }
        failureReason = reason;
        status = SagaStatus.COMPENSATING;
        compensateNext(now);
    }

    private void compensateNext(final Instant now) {
        while (!completedSteps.isEmpty()) {
            var compensation = completedSteps.pop().compensation();
            if (compensation != null) {
                beginStep(compensation, now);
                return;
            }
        }
        finish(SagaStatus.COMPENSATED);
    }

    public boolean isTimedOut(final Instant now, final Duration timeout) {
        return status.isActive() && stepStartedAt.plus(timeout).isBefore(now);
    }

    public void retry(final Instant now) {
        attempts++;
        stepStartedAt = now;
    }

    private void beginStep(final SagaStep step, final Instant now) {
        currentStep = step;
        stepStartedAt = now;
        attempts = 0;
    }

    private void finish(final SagaStatus finalStatus) {
        status = finalStatus;
        currentStep = null;
    }

    private void requireAwaiting(final SagaStep step) {
        if (!awaits(step)) {
            throw new IllegalStateException("Saga %s does not await %s".formatted(orderId.value(), step));
        }
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public SagaStep getCurrentStep() {
        return currentStep;
    }

    // od najstarszego do najnowszego
    public List<SagaStep> getCompletedSteps() {
        return completedSteps.reversed().stream().toList();
    }

    public Instant getStepStartedAt() {
        return stepStartedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getFailureReason() {
        return failureReason;
    }

}
