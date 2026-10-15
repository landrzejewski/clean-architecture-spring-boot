package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.Price;

import static java.util.Objects.requireNonNull;

public record PlaceOrderCommand(String description, CardReference cardReference, Price total) {

    public PlaceOrderCommand {
        requireNonNull(description);
        requireNonNull(cardReference);
        requireNonNull(total);
    }

}
