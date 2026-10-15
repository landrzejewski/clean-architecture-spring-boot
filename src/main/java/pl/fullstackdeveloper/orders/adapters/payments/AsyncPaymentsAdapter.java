package pl.fullstackdeveloper.orders.adapters.payments;

import org.springframework.context.ApplicationEventPublisher;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.Payments;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.Price;
import pl.fullstackdeveloper.payments.api.PaymentRequested;
import pl.fullstackdeveloper.payments.api.RefundRequested;

// Polecenia sagi dla płatności - publikowane w transakcji orkiestratora (outbox)
@Adapter
class AsyncPaymentsAdapter implements Payments {

    private final ApplicationEventPublisher eventPublisher;

    AsyncPaymentsAdapter(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void requestPayment(final OrderId orderId, final CardReference card, final Price price) {
        eventPublisher.publishEvent(new PaymentRequested(orderId.value().toString(), card.value(), price.amount(),
                price.currency().getCurrencyCode()));
    }

    @Override
    public void requestRefund(final OrderId orderId, final CardReference card, final Price price) {
        eventPublisher.publishEvent(new RefundRequested(orderId.value().toString(), card.value(), price.amount(),
                price.currency().getCurrencyCode()));
    }

}
