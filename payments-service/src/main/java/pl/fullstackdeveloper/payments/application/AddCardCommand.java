package pl.fullstackdeveloper.payments.application;

import java.util.Currency;

import static java.util.Objects.requireNonNull;

// Self-validating command - niepoprawnej komendy nie da się nawet utworzyć
public record AddCardCommand(Currency currency) {

    public AddCardCommand {
        requireNonNull(currency, "currency is required");
    }

}
