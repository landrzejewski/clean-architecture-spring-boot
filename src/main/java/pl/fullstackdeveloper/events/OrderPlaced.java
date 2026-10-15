package pl.fullstackdeveloper.events;

import java.math.BigDecimal;

// Zamówienie złożone - startuje proces
public record OrderPlaced(String orderId, String cardNumber, BigDecimal amount, String currencyCode, String address) {
}
