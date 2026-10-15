package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.ExchangeRate;

import java.util.Currency;

public interface ExchangeRateProvider {

    ExchangeRate getRate(Currency from, Currency to);

}
