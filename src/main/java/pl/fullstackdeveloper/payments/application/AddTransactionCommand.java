package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.Money;
import pl.fullstackdeveloper.payments.domain.TransactionType;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record AddTransactionCommand(CardNumber cardNumber, Money value, TransactionType type) {

    public AddTransactionCommand {
        requireNonNull(cardNumber, "cardNumber is required");
        requireNonNull(value, "value is required");
        requireNonNull(type, "type is required");
        if (value.amount().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Transaction value must be positive");
        }
    }

}
