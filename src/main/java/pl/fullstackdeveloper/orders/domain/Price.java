package pl.fullstackdeveloper.orders.domain;

import java.math.BigDecimal;
import java.util.Currency;

// Własny Value Object zamówień - orders nie dzieli już modelu z payments
public record Price(BigDecimal amount, Currency currency) {

    public Price {
        if (amount == null || currency == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }

    public static Price of(final String amount, final String currencyCode) {
        return new Price(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

}
