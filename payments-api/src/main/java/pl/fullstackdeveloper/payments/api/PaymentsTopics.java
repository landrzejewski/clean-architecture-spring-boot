package pl.fullstackdeveloper.payments.api;

// Nazwy topiców Kafka są częścią kontraktu
public final class PaymentsTopics {

    public static final String PAYMENT_REQUESTED = "payments.payment-requested";
    public static final String PAYMENT_PROCESSED = "payments.payment-processed";

    private PaymentsTopics() {
    }

}
