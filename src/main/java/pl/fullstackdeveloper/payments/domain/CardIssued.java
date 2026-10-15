package pl.fullstackdeveloper.payments.domain;

import java.time.LocalDate;
import java.util.Currency;

public record CardIssued(CardId cardId, CardNumber cardNumber, LocalDate expiration, Currency currency) implements CardEvent {
}
