package pl.fullstackdeveloper.payments.api;

import java.math.BigDecimal;

/*
 Żądanie zwrotu środków - KOMPENSACJA płatności w sadze. To nie jest techniczny rollback
 (płatność została zatwierdzona i może być już widoczna dla klienta), tylko nowa operacja biznesowa,
 która semantycznie odwraca skutek poprzedniej.
 */
public record RefundRequested(String reference, String cardNumber, BigDecimal amount, String currencyCode) {
}
