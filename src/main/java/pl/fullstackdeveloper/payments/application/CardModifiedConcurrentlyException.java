package pl.fullstackdeveloper.payments.application;

// Część kontraktu portu CardRepository - adapter tłumaczy na ten wyjątek błędy optimistic locking swojej technologii
public final class CardModifiedConcurrentlyException extends RuntimeException {

    public CardModifiedConcurrentlyException(final Throwable cause) {
        super("Card was modified concurrently", cause);
    }

}
