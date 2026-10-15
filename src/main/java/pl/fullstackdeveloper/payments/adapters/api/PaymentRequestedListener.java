package pl.fullstackdeveloper.payments.adapters.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.api.*;

/*
 Kroki sagi po stronie płatności: obciążenie karty i jego kompensacja (zwrot).
 Oba są idempotentne (ProcessedMessages) - saga może je bezpiecznie ponawiać (retry, timeout, restart).
 */
@Adapter
class PaymentRequestedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestedListener.class);

    private final PaymentsApi paymentsApi;
    private final ProcessedMessages processedMessages;
    private final ApplicationEventPublisher eventPublisher;

    PaymentRequestedListener(final PaymentsApi paymentsApi, final ProcessedMessages processedMessages,
                             final ApplicationEventPublisher eventPublisher) {
        this.paymentsApi = paymentsApi;
        this.processedMessages = processedMessages;
        this.eventPublisher = eventPublisher;
    }

    @ApplicationModuleListener
    void on(final PaymentRequested request) {
        LOGGER.info("Processing payment request {}", request.reference());
        var result = processedMessages.processOnce("payment:" + request.reference(), () ->
                paymentsApi.pay(new PaymentRequest(request.cardNumber(), request.amount(), request.currencyCode())));
        eventPublisher.publishEvent(new PaymentProcessed(request.reference(), result));
    }

    @ApplicationModuleListener
    void on(final RefundRequested request) {
        LOGGER.info("Processing refund request {}", request.reference());
        var result = processedMessages.processOnce("refund:" + request.reference(), () ->
                paymentsApi.refund(new PaymentRequest(request.cardNumber(), request.amount(), request.currencyCode())));
        eventPublisher.publishEvent(new RefundProcessed(request.reference(), result));
    }

}
