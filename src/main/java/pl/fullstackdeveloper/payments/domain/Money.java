package pl.fullstackdeveloper.payments.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

import static java.math.BigDecimal.ZERO;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Amount and currency are required");
        }
        if (amount.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be greater than or equal zero");
        }
        // Normalizacja skali sprawia, że 10 PLN == 10.00 PLN (BigDecimal.equals porównuje też skalę)
        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_EVEN);
    }

    public static Money of(final String amount, final String currencyCode) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public static Money zero(final Currency currency) {
        return new Money(ZERO, currency);
    }

    public Money add(final Money money) {
        checkCurrency(money);
        return new Money(amount.add(money.amount), currency);
    }

    public Money subtract(final Money money) {
        checkCurrency(money);
        return new Money(amount.subtract(money.amount), currency);
    }

    public boolean isGreaterOrEqual(final Money money) {
        checkCurrency(money);
        return amount.compareTo(money.amount) >= 0;
    }

    public boolean hasCurrency(final Currency currency) {
        return this.currency.equals(currency);
    }

    private void checkCurrency(final Money money) {
        if (!hasCurrency(money.currency)) {
            throw new MismatchedCurrencyException();
        }
    }

}
