package pl.fullstackdeveloper.payments.adapters.api;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.api.PaymentRequested;
import pl.fullstackdeveloper.payments.api.PaymentsTopics;
import tools.jackson.databind.json.JsonMapper;

/*
 Wejście z Kafki ("inbox"): komunikat zamieniamy na wewnętrzne zdarzenie aplikacyjne publikowane
 w transakcji. Dalej działa niezmieniony PaymentRequestedListener (@ApplicationModuleListener),
 a Event Publication Registry gwarantuje, że przetwarzanie zostanie ponowione w razie błędu.
 */
@Adapter
class KafkaPaymentRequestsListener {

    private final JsonMapper jsonMapper;
    private final ApplicationEventPublisher eventPublisher;

    KafkaPaymentRequestsListener(final JsonMapper jsonMapper, final ApplicationEventPublisher eventPublisher) {
        this.jsonMapper = jsonMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @KafkaListener(topics = PaymentsTopics.PAYMENT_REQUESTED, groupId = "payments-service")
    void on(final String message) {
        eventPublisher.publishEvent(jsonMapper.readValue(message, PaymentRequested.class));
    }

}
