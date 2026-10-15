package pl.fullstackdeveloper.payments.adapters.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.api.PaymentProcessed;
import pl.fullstackdeveloper.payments.api.PaymentRequest;
import pl.fullstackdeveloper.payments.api.PaymentRequested;
import pl.fullstackdeveloper.payments.api.PaymentsApi;

/*
 @ApplicationModuleListener = @Async + @Transactional(REQUIRES_NEW) + @TransactionalEventListener(AFTER_COMMIT).
 Listener działa w nowej transakcji, w której: obciążamy kartę, publikujemy PaymentProcessed (zapis w
 Event Publication Registry) i oznaczamy przetworzenie PaymentRequested - wszystko albo nic.
 Gdy listener rzuci wyjątek, publikacja zostaje "niekompletna" i zostanie ponowiona (po restarcie
 lub przez IncompleteEventPublications) - stąd wymóg idempotentności przy dostarczaniu at-least-once.
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

    @ApplicationModuleListener
    void on(final PaymentRequested request) {
        LOGGER.info("Processing payment request {}", request.reference());
        var result = paymentsApi.pay(new PaymentRequest(request.cardNumber(), request.amount(), request.currencyCode()));
        eventPublisher.publishEvent(PaymentProcessed.of(request.reference(), result));
    }

}
