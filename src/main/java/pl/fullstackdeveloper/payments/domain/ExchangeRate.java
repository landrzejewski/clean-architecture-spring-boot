package pl.fullstackdeveloper.payments.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Currency;

// Kurs wymiany jako Value Object - pojęcie domenowe, niezależne od źródła (NBP, ECB, plik...)
public record ExchangeRate(Currency from, Currency to, BigDecimal rate) {

    public ExchangeRate {
        if (rate.signum() <= 0) {
            throw new IllegalArgumentException("Exchange rate must be positive");
        }
    }

    public Money convert(final Money money) {
        if (!money.hasCurrency(from)) {
            throw new MismatchedCurrencyException();
        }
        return new Money(money.amount().multiply(rate, MathContext.DECIMAL64), to);
    }

}
