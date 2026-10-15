package pl.fullstackdeveloper.orders.domain;

public enum SagaStep {

    CHARGE_PAYMENT,
    SCHEDULE_SHIPMENT,
    REFUND_PAYMENT;

    // Krok kompensujący dany krok (null = krok nie wymaga kompensacji)
    SagaStep compensation() {
        return switch (this) {
            case CHARGE_PAYMENT -> REFUND_PAYMENT;
            case SCHEDULE_SHIPMENT, REFUND_PAYMENT -> null;
        };
    }

}
