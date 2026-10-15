package pl.fullstackdeveloper.orders.adapters.persistence;

import jakarta.persistence.*;
import pl.fullstackdeveloper.orders.domain.SagaStatus;
import pl.fullstackdeveloper.orders.domain.SagaStep;

import java.time.Instant;

@Entity
@Table(name = "order_sagas")
class OrderSagaEntity {

    @Id
    private String orderId;
    @Enumerated(EnumType.STRING)
    private SagaStatus status;
    @Enumerated(EnumType.STRING)
    private SagaStep currentStep;
    private String completedSteps;
    private Instant stepStartedAt;
    private int attempts;
    private String failureReason;
    @Version
    private long version;

    protected OrderSagaEntity() {
    }

    OrderSagaEntity(final String orderId) {
        this.orderId = orderId;
    }

    String getOrderId() {
        return orderId;
    }

    SagaStatus getStatus() {
        return status;
    }

    void setStatus(final SagaStatus status) {
        this.status = status;
    }

    SagaStep getCurrentStep() {
        return currentStep;
    }

    void setCurrentStep(final SagaStep currentStep) {
        this.currentStep = currentStep;
    }

    String getCompletedSteps() {
        return completedSteps;
    }

    void setCompletedSteps(final String completedSteps) {
        this.completedSteps = completedSteps;
    }

    Instant getStepStartedAt() {
        return stepStartedAt;
    }

    void setStepStartedAt(final Instant stepStartedAt) {
        this.stepStartedAt = stepStartedAt;
    }

    int getAttempts() {
        return attempts;
    }

    void setAttempts(final int attempts) {
        this.attempts = attempts;
    }

    String getFailureReason() {
        return failureReason;
    }

    void setFailureReason(final String failureReason) {
        this.failureReason = failureReason;
    }

}
