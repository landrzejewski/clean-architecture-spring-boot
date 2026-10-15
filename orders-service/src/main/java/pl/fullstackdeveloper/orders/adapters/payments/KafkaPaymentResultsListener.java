package pl.fullstackdeveloper.orders.adapters.payments;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.api.PaymentProcessed;
import pl.fullstackdeveloper.payments.api.PaymentsTopics;
import tools.jackson.databind.json.JsonMapper;

// Wejście z Kafki ("inbox") - dalej działa niezmieniony PaymentProcessedListener z 15
@Adapter
class KafkaPaymentResultsListener {

    private final JsonMapper jsonMapper;
    private final ApplicationEventPublisher eventPublisher;

    KafkaPaymentResultsListener(final JsonMapper jsonMapper, final ApplicationEventPublisher eventPublisher) {
        this.jsonMapper = jsonMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @KafkaListener(topics = PaymentsTopics.PAYMENT_PROCESSED, groupId = "orders-service")
    void on(final String message) {
        eventPublisher.publishEvent(jsonMapper.readValue(message, PaymentProcessed.class));
    }

}
