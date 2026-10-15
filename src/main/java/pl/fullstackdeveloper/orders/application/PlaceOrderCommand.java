package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.Price;

import static java.util.Objects.requireNonNull;

public record PlaceOrderCommand(String description, CardReference cardReference, Price total, String address) {

    public PlaceOrderCommand {
        requireNonNull(description);
        requireNonNull(cardReference);
        requireNonNull(total);
        requireNonNull(address);
    }

}
