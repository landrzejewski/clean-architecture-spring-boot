package pl.fullstackdeveloper.events;

// Obciążenie karty odrzucone - proces kończy się bez kompensacji
public record PaymentFailed(String orderId, String reason) {
}
