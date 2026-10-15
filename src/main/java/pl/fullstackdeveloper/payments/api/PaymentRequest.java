package pl.fullstackdeveloper.payments.api;

import java.math.BigDecimal;

public record PaymentRequest(String cardNumber, BigDecimal amount, String currencyCode) {
}
