package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.CardNumber;

public interface CardNumberGenerator {

    CardNumber getNext();

}
