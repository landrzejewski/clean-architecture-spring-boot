package pl.fullstackdeveloper.payments.domain;

public final class MismatchedCurrencyException extends DomainException {

    public MismatchedCurrencyException() {
        super("Mismatched currency");
    }

}
