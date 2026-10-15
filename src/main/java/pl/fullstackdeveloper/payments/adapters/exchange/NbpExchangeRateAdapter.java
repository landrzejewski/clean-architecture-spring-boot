package pl.fullstackdeveloper.payments.adapters.exchange;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.client.RestClient;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.ExchangeRateProvider;
import pl.fullstackdeveloper.payments.domain.ExchangeRate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Currency;
import java.util.List;

/*
 Adapter wyjściowy do zewnętrznego API (Narodowy Bank Polski) pełniący rolę Anti-Corruption Layer:
 model NBP (tabela A, kurs średni "mid" w PLN za jednostkę waluty) nie przecieka do aplikacji -
 na zewnątrz wychodzi wyłącznie domenowy ExchangeRate.
 */
@Adapter
@ConditionalOnProperty(name = "payments.exchange-rates.provider", havingValue = "nbp")
class NbpExchangeRateAdapter implements ExchangeRateProvider {

    private static final Currency PLN = Currency.getInstance("PLN");

    private final RestClient restClient;

    NbpExchangeRateAdapter(final RestClient.Builder builder,
                           @Value("${payments.exchange-rates.nbp-url:https://api.nbp.pl/api}") final String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public ExchangeRate getRate(final Currency from, final Currency to) {
        var rate = plnPerUnit(from).divide(plnPerUnit(to), MathContext.DECIMAL64);
        return new ExchangeRate(from, to, rate);
    }

    private BigDecimal plnPerUnit(final Currency currency) {
        if (currency.equals(PLN)) {
            return BigDecimal.ONE;
        }
        var table = restClient.get()
                .uri("/exchangerates/rates/a/{code}?format=json", currency.getCurrencyCode().toLowerCase())
                .retrieve()
                .body(NbpRateTable.class);
        if (table == null || table.rates().isEmpty()) {
            throw new IllegalStateException("No exchange rate for " + currency);
        }
        return table.rates().getFirst().mid();
    }

    // Model zewnętrznego API - widoczny tylko w adapterze
    record NbpRateTable(String code, List<NbpRate> rates) {
    }

    record NbpRate(String effectiveDate, BigDecimal mid) {
    }

}
