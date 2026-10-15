package pl.fullstackdeveloper.payments.domain;

import java.time.LocalDate;
import java.util.Currency;

// Migawka stanu (Memento) - pozwala nie odtwarzać agregatu od pierwszego zdarzenia
public record CardSnapshot(CardId id, CardNumber number, LocalDate expiration, Currency currency, Money balance,
                           long transactionsCount, long version) {
}
