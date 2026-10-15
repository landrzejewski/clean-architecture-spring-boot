package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.CardNumber;

public interface GetCardUseCase {

    CardView handle(CardNumber cardNumber);

}
