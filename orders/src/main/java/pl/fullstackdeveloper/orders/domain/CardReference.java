package pl.fullstackdeveloper.orders.domain;

// Z punktu widzenia zamówień karta to tylko referencja (identyfikator w innym kontekście)
public record CardReference(String value) {

    public CardReference {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Card reference is required");
        }
    }

}
