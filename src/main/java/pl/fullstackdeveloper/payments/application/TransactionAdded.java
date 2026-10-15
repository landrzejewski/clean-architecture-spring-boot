package pl.fullstackdeveloper.payments.application;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/*
 Zdarzenie aplikacyjne (integracyjne) - publikowane na zewnątrz, dlatego zawiera wyłącznie typy proste.
 Zdarzenie domenowe (TransactionRegistered) zostaje wewnątrz modułu.
 */
public record TransactionAdded(String cardNumber, String transactionId, String type,
                               BigDecimal amount, String currencyCode, ZonedDateTime timestamp)
        implements PaymentsEvent {
}
