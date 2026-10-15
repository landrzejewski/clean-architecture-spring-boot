package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.Price;

/*
 Port wyjściowy zamówień - zdefiniowany w języku zamówień, nie płatności.
 Wynik płatności wraca osobnym kanałem (CompletePaymentUseCase), dzięki czemu
 adapter może być synchroniczny albo asynchroniczny, a use case tego nie zauważy.
 */
public interface Payments {

    void requestPayment(OrderId orderId, CardReference card, Price price);

}
