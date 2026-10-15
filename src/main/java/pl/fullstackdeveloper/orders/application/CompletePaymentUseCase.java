package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.OrderId;

public interface CompletePaymentUseCase {

    void handle(OrderId orderId, PaymentOutcome outcome);

}
