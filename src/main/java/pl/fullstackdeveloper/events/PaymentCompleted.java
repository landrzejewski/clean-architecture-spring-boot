package pl.fullstackdeveloper.events;

// Karta obciążona
public record PaymentCompleted(String orderId, String paymentId) {
}
