package pl.fullstackdeveloper.payments.domain;

public abstract class DomainException extends RuntimeException {

    protected DomainException(final String message) {
        super(message);
    }

}
