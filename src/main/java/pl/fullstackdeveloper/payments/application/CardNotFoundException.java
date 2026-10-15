package pl.fullstackdeveloper.payments.application;

public final class CardNotFoundException extends RuntimeException {

    public CardNotFoundException() {
        super("Card not found");
    }

}
