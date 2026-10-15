package pl.fullstackdeveloper.payments.api;

/*
 Zdarzenie integracyjne publikowane przez płatności po przetworzeniu PaymentRequested.
 Po wydzieleniu usługi kontrakt przechodzi przez sieć (JSON w Kafce), dlatego zamiast zagnieżdżonej
 hierarchii sealed ma płaską, jawną strukturę - kontrakt sieciowy musi być prosty i wersjonowalny.
 */
public record PaymentProcessed(String reference, String status, String paymentId, String reason) {

    public static final String COMPLETED = "COMPLETED";
    public static final String REJECTED = "REJECTED";

    public static PaymentProcessed of(final String reference, final PaymentResult result) {
        return switch (result) {
            case PaymentResult.Completed completed -> new PaymentProcessed(reference, COMPLETED, completed.paymentId(), null);
            case PaymentResult.Rejected rejected -> new PaymentProcessed(reference, REJECTED, null, rejected.reason());
        };
    }

    public PaymentResult toResult() {
        return COMPLETED.equals(status) ? new PaymentResult.Completed(paymentId) : new PaymentResult.Rejected(reason);
    }

}
