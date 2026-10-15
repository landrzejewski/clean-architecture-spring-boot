package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.Money;

import static java.util.Objects.requireNonNull;

public record PlaceOrderCommand(String description, CardNumber cardNumber, Money total) {

    public PlaceOrderCommand {
        requireNonNull(description);
        requireNonNull(cardNumber);
        requireNonNull(total);
    }

}
