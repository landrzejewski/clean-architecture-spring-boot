package pl.fullstackdeveloper.shipping.domain;

public final class UndeliverableAddressException extends RuntimeException {

    public UndeliverableAddressException() {
        super("Address is outside of the delivery area");
    }

}
