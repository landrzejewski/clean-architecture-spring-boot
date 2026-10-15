package pl.fullstackdeveloper.shipping.domain;

import java.util.Set;

public record Address(String value) {

    // Reguła biznesowa na potrzeby demo sagi: do tych regionów nie wysyłamy
    private static final Set<String> UNSUPPORTED_REGIONS = Set.of("antarctica", "mars");

    public Address {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Address is required");
        }
    }

    public boolean isDeliverable() {
        var normalized = value.toLowerCase();
        return UNSUPPORTED_REGIONS.stream().noneMatch(normalized::contains);
    }

}
