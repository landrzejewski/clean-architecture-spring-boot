package pl.fullstackdeveloper.orders.adapters.payments;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pl.fullstackdeveloper.orders.application.PaymentOutcome;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.Price;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/*
 Strona konsumenta kontraktu: adapter wysyła żądanie zgodne z plikiem kontraktu
 i poprawnie interpretuje odpowiedzi z plików kontraktu.
 */
class PaymentsContractConsumerTest {

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final List<PaymentOutcome> outcomes = new ArrayList<>();
    private final HttpPaymentsAdapter adapter = new HttpPaymentsAdapter(builder, "http://payments", (orderId, outcome) -> outcomes.add(outcome));

    @Test
    void completed_payment_from_contract_is_understood() throws Exception {
        expectPayment("pay-response-completed.json");

        adapter.requestPayment(new OrderId(), new CardReference("1111222233334444"), Price.of("40.00", "PLN"));

        assertThat(outcomes).containsExactly(new PaymentOutcome.Paid("8b1f8a62-3f1a-4bd4-9f39-2e5b2d0b8c11"));
        server.verify();
    }

    @Test
    void rejected_payment_from_contract_is_understood() throws Exception {
        expectPayment("pay-response-rejected.json");

        adapter.requestPayment(new OrderId(), new CardReference("1111222233334444"), Price.of("40.00", "PLN"));

        assertThat(outcomes).containsExactly(new PaymentOutcome.Declined("Insufficient balance"));
    }

    private void expectPayment(final String response) throws Exception {
        server.expect(requestTo("http://payments/api/payments"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(contract("pay-request.json"), true))
                .andRespond(withSuccess(contract(response), MediaType.APPLICATION_JSON));
    }

    private static String contract(final String name) throws Exception {
        return new ClassPathResource("contracts/" + name).getContentAsString(StandardCharsets.UTF_8);
    }

}
