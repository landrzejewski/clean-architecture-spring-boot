package pl.fullstackdeveloper.payments.adapters.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.api.PaymentProcessed;
import pl.fullstackdeveloper.payments.api.PaymentRequest;
import pl.fullstackdeveloper.payments.api.PaymentRequested;
import pl.fullstackdeveloper.payments.api.PaymentsApi;

/*
 Asynchroniczny adapter wejściowy płatności: odbiera PaymentRequested (w osobnym wątku),
 realizuje płatność tym samym kodem co wywołanie synchroniczne i odsyła PaymentProcessed.

 Problem do zapamiętania: jeśli aplikacja padnie między zatwierdzeniem płatności a publikacją
 PaymentProcessed, zdarzenie zginie, a zamówienie na zawsze zostanie w AWAITING_PAYMENT.
 Rozwiązanie (outbox / Event Publication Registry) w 15-spring-modulith.
 */
@Adapter
class PaymentRequestedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestedListener.class);

    private final PaymentsApi paymentsApi;
    private final ApplicationEventPublisher eventPublisher;

    PaymentRequestedListener(final PaymentsApi paymentsApi, final ApplicationEventPublisher eventPublisher) {
        this.paymentsApi = paymentsApi;
        this.eventPublisher = eventPublisher;
    }

    @Async
    @EventListener
    void on(final PaymentRequested request) {
        LOGGER.info("Processing payment request {}", request.reference());
        var result = paymentsApi.pay(new PaymentRequest(request.cardNumber(), request.amount(), request.currencyCode()));
        eventPublisher.publishEvent(new PaymentProcessed(request.reference(), result));
    }

}
