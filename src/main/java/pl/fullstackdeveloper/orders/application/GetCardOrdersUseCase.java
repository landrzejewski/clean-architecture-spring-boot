package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.List;

public interface GetCardOrdersUseCase {

    List<OrderView> handle(CardNumber cardNumber);

}
