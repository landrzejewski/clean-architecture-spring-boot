package pl.fullstackdeveloper.payments.api;

public record RefundProcessed(String reference, PaymentResult result) {
}
