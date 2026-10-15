package pl.fullstackdeveloper.orders.adapters.payments;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.CompletePaymentUseCase;
import pl.fullstackdeveloper.orders.application.Payments;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.Price;
import pl.fullstackdeveloper.payments.api.PaymentRequest;
import pl.fullstackdeveloper.payments.api.PaymentsApi;

// Wariant synchroniczny: wywołanie PaymentsApi w tym samym wątku i natychmiastowe przekazanie wyniku
@Adapter
@ConditionalOnProperty(name = "orders.payments.mode", havingValue = "sync")
class SyncPaymentsAdapter implements Payments {

    private final PaymentsApi paymentsApi;
    private final CompletePaymentUseCase completePayment;

    SyncPaymentsAdapter(final PaymentsApi paymentsApi, final CompletePaymentUseCase completePayment) {
        this.paymentsApi = paymentsApi;
        this.completePayment = completePayment;
    }

    @Override
    public void requestPayment(final OrderId orderId, final CardReference card, final Price price) {
        var result = paymentsApi.pay(new PaymentRequest(card.value(), price.amount(), price.currency().getCurrencyCode()));
        completePayment.handle(orderId, PaymentsTranslator.toOutcome(result));
    }

}
