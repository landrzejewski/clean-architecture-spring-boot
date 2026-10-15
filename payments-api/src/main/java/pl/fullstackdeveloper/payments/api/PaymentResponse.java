package pl.fullstackdeveloper.payments.api;

// Model odpowiedzi HTTP (POST /api/payments) - ta sama informacja co PaymentResult, w postaci przyjaznej dla JSON
public record PaymentResponse(String status, String paymentId, String reason) {

    public static PaymentResponse from(final PaymentResult result) {
        var event = PaymentProcessed.of(null, result);
        return new PaymentResponse(event.status(), event.paymentId(), event.reason());
    }

    public PaymentResult toResult() {
        return new PaymentProcessed(null, status, paymentId, reason).toResult();
    }

}
