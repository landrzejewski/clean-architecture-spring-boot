package pl.fullstackdeveloper.payments.domain;

import java.util.UUID;

public record CardId(UUID value) {

    public CardId() {
        this(UUID.randomUUID());
    }

}
