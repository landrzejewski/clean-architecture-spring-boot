package pl.fullstackdeveloper.orders.domain;

public enum SagaStatus {

    RUNNING,
    COMPENSATING,
    COMPLETED,
    COMPENSATED,
    FAILED;

    public boolean isActive() {
        return this == RUNNING || this == COMPENSATING;
    }

}
