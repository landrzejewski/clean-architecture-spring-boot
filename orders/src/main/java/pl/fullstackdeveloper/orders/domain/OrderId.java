package pl.fullstackdeveloper.orders.domain;

import java.util.UUID;

public record OrderId(UUID value) {

    public OrderId() {
        this(UUID.randomUUID());
    }

}
