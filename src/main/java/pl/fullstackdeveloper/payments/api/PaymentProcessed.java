package pl.fullstackdeveloper.payments.api;

// Zdarzenie integracyjne publikowane przez płatności po przetworzeniu PaymentRequested
public record PaymentProcessed(String reference, PaymentResult result) {
}
