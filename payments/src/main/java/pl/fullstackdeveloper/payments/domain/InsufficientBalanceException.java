package pl.fullstackdeveloper.payments.domain;

public final class InsufficientBalanceException extends DomainException {

    public InsufficientBalanceException() {
        super("Insufficient balance");
    }

}
