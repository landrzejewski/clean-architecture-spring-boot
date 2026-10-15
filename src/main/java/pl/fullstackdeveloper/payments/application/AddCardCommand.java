package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.common.cqrs.Command;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.Currency;

import static java.util.Objects.requireNonNull;

// Self-validating command - niepoprawnej komendy nie da się nawet utworzyć
public record AddCardCommand(Currency currency) implements Command<CardNumber> {

    public AddCardCommand {
        requireNonNull(currency, "currency is required");
    }

}
