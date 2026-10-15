package pl.fullstackdeveloper.payments.adapters.exchange;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/*
 Test adaptera zewnętrznego API bez sieci - serwer HTTP zastąpiony MockRestServiceServer.
 Sprawdzamy tłumaczenie modelu NBP na domenowy ExchangeRate (ACL).
 */
class NbpExchangeRateAdapterTest {

    private static final String BASE_URL = "https://nbp.test/api";

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final NbpExchangeRateAdapter adapter = new NbpExchangeRateAdapter(builder, BASE_URL);

    @Test
    void rate_to_pln_is_taken_from_nbp_table() {
        expectRate("eur", "4.2500");

        var rate = adapter.getRate(Currency.getInstance("EUR"), Currency.getInstance("PLN"));

        assertThat(rate.rate()).isEqualByComparingTo("4.25");
        server.verify();
    }

    @Test
    void cross_rate_is_calculated_via_pln() {
        expectRate("eur", "4.00");
        expectRate("usd", "3.20");

        var rate = adapter.getRate(Currency.getInstance("EUR"), Currency.getInstance("USD"));

        assertThat(rate.rate()).isEqualByComparingTo(new BigDecimal("1.25"));
    }

    private void expectRate(final String code, final String mid) {
        server.expect(requestTo(BASE_URL + "/exchangerates/rates/a/" + code + "?format=json"))
                .andRespond(withSuccess("""
                        {"table":"A","currency":"x","code":"%s","rates":[{"no":"1/A/NBP/2026","effectiveDate":"2026-10-15","mid":%s}]}
                        """.formatted(code.toUpperCase(), mid), MediaType.APPLICATION_JSON));
    }

}
