package pl.fullstackdeveloper.shipping.domain;

import java.util.UUID;

public record ShipmentId(UUID value) {

    public ShipmentId() {
        this(UUID.randomUUID());
    }

}
