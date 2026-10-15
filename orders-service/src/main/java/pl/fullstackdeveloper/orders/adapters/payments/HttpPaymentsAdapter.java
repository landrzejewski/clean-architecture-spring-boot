package pl.fullstackdeveloper.orders.adapters.payments;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.CompletePaymentUseCase;
import pl.fullstackdeveloper.orders.application.Payments;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.Price;
import pl.fullstackdeveloper.payments.api.PaymentRequest;
import pl.fullstackdeveloper.payments.api.PaymentResponse;

/*
 Następca SyncPaymentsAdapter z 15: zamiast wywołania w procesie - HTTP do payments-service.
 Sieć zawodzi, więc dochodzą: retry (tylko dla błędów połączenia) i limit współbieżności
 (Spring Framework 7: @Retryable, @ConcurrencyLimit). Use case PlaceOrder nie zmienił się.
 */
@Adapter
@ConditionalOnProperty(name = "orders.payments.mode", havingValue = "sync")
class HttpPaymentsAdapter implements Payments {

    private final RestClient restClient;
    private final CompletePaymentUseCase completePayment;

    HttpPaymentsAdapter(final RestClient.Builder builder, @Value("${orders.payments.url}") final String paymentsUrl,
                        final CompletePaymentUseCase completePayment) {
        this.restClient = builder.baseUrl(paymentsUrl).build();
        this.completePayment = completePayment;
    }

    @Override
    @Retryable(includes = ResourceAccessException.class, maxRetries = 3, delay = 200, multiplier = 2)
    @ConcurrencyLimit(10)
    public void requestPayment(final OrderId orderId, final CardReference card, final Price price) {
        var request = new PaymentRequest(card.value(), price.amount(), price.currency().getCurrencyCode());
        var response = restClient.post()
                .uri("/api/payments")
                .body(request)
                .retrieve()
                .body(PaymentResponse.class);
        completePayment.handle(orderId, PaymentsTranslator.toOutcome(response));
    }

}
