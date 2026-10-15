package pl.fullstackdeveloper.orders.application;

// Wynik płatności w języku zamówień (tłumaczony z PaymentResult przez adapter - ACL)
public sealed interface PaymentOutcome {

    record Paid(String paymentId) implements PaymentOutcome {
    }

    record Declined(String reason) implements PaymentOutcome {
    }

}
