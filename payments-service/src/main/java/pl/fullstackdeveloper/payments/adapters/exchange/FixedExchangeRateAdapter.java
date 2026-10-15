package pl.fullstackdeveloper.payments.adapters.exchange;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.ExchangeRateProvider;
import pl.fullstackdeveloper.payments.domain.ExchangeRate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Currency;
import java.util.Map;

// Domyślny adapter offline (stałe kursy) - ten sam port, inne źródło danych
@Adapter
@ConditionalOnProperty(name = "payments.exchange-rates.provider", havingValue = "fixed", matchIfMissing = true)
class FixedExchangeRateAdapter implements ExchangeRateProvider {

    private static final Map<String, BigDecimal> PLN_PER_UNIT = Map.of(
            "PLN", BigDecimal.ONE,
            "EUR", new BigDecimal("4.25"),
            "USD", new BigDecimal("3.90"),
            "GBP", new BigDecimal("5.00"));

    @Override
    public ExchangeRate getRate(final Currency from, final Currency to) {
        var rate = plnPerUnit(from).divide(plnPerUnit(to), MathContext.DECIMAL64);
        return new ExchangeRate(from, to, rate);
    }

    private BigDecimal plnPerUnit(final Currency currency) {
        var rate = PLN_PER_UNIT.get(currency.getCurrencyCode());
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency " + currency);
        }
        return rate;
    }

}
