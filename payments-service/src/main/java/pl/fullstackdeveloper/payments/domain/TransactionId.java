package pl.fullstackdeveloper.payments.domain;

import java.util.UUID;

public record TransactionId(UUID value) {

    public TransactionId() {
        this(UUID.randomUUID());
    }

}
