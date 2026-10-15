package pl.fullstackdeveloper.payments.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyExchangeTest {

    private static final ExchangeRate EUR_TO_PLN = new ExchangeRate(Currency.getInstance("EUR"), Currency.getInstance("PLN"), new BigDecimal("4.25"));

    private final CurrencyExchange currencyExchange = new CurrencyExchange();

    @Test
    void foreign_inflow_is_converted_without_fee() {
        assertThat(currencyExchange.toCardCurrency(Money.of("10", "EUR"), EUR_TO_PLN, TransactionType.INFLOW))
                .isEqualTo(Money.of("42.50", "PLN"));
    }

    @Test
    void foreign_payment_is_charged_with_two_percent_fee() {
        assertThat(currencyExchange.toCardCurrency(Money.of("10", "EUR"), EUR_TO_PLN, TransactionType.PAYMENT))
                .isEqualTo(Money.of("43.35", "PLN"));
    }

}
