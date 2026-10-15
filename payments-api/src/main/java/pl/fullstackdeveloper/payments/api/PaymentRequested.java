package pl.fullstackdeveloper.payments.api;

import java.math.BigDecimal;

/*
 Komunikat asynchroniczny do modułu płatności. "reference" to identyfikator korelacji nadawcy
 (np. id zamówienia) - płatności go nie interpretują, tylko odsyłają w PaymentProcessed.
 */
public record PaymentRequested(String reference, String cardNumber, BigDecimal amount, String currencyCode) {
}
