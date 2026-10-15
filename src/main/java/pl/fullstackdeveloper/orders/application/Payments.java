package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.Price;

// Port wyjściowy - polecenia dla płatności w języku zamówień; odpowiedzi trafiają do OrderSagaOrchestrator
public interface Payments {

    void requestPayment(OrderId orderId, CardReference card, Price price);

    void requestRefund(OrderId orderId, CardReference card, Price price);

}
