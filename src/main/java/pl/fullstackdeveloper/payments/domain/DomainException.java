package pl.fullstackdeveloper.payments.domain;

// sealed: lista wyjątków domenowych jest zamknięta i znana - nikt spoza domeny nie doda nowego
public abstract sealed class DomainException extends RuntimeException
        permits InsufficientBalanceException, MismatchedCurrencyException, InvalidTransactionTimestampException {

    protected DomainException(final String message) {
        super(message);
    }

}
