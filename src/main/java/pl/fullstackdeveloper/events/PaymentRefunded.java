package pl.fullstackdeveloper.events;

// Środki zwrócone - zamówienie zostaje anulowane
public record PaymentRefunded(String orderId, String reason) {
}
