package pl.fullstackdeveloper.payments.application;

public interface TransactionEventPublisher {

    void publish(TransactionAdded event);

}
