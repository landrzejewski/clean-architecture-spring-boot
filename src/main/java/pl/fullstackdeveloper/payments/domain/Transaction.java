package pl.fullstackdeveloper.payments.domain;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Currency;

public record Transaction(TransactionId id, ZonedDateTime timestamp, Money value, TransactionType type) {

    public boolean hasCurrency(final Currency currency) {
        return value.hasCurrency(currency);
    }

    public boolean isBefore(final LocalDate date) {
        return timestamp.toLocalDate().isBefore(date);
    }

}
