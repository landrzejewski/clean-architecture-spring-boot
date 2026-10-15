package pl.fullstackdeveloper.payments.domain;

public final class InvalidTransactionTimestampException extends DomainException {

    public InvalidTransactionTimestampException() {
        super("Transaction timestamp is after card expiration");
    }

}
