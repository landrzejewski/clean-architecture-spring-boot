package pl.fullstackdeveloper.payments.domain;

import java.math.BigDecimal;
import java.math.MathContext;

/*
 Usługa domenowa - reguła, która nie należy naturalnie do żadnego agregatu.
 Wymaganie z ćwiczenia w 00: prowizja 2% za płatność w walucie innej niż waluta karty.
 */
public final class CurrencyExchange {

    private static final BigDecimal FOREIGN_PAYMENT_FEE = new BigDecimal("1.02");

    public Money toCardCurrency(final Money value, final ExchangeRate rate, final TransactionType type) {
        var converted = rate.convert(value);
        return switch (type) {
            case INFLOW -> converted;
            case PAYMENT -> new Money(converted.amount().multiply(FOREIGN_PAYMENT_FEE, MathContext.DECIMAL64), converted.currency());
        };
    }

}
