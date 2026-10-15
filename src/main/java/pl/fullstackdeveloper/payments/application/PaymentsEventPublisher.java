package pl.fullstackdeveloper.payments.application;

public interface PaymentsEventPublisher {

    void publish(PaymentsEvent event);

}
