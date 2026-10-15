package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.CardReference;

import java.util.List;

public interface GetCardOrdersUseCase {

    List<OrderView> handle(CardReference cardReference);

}
