package pl.fullstackdeveloper.payments.adapters.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.PaymentsEvent;
import pl.fullstackdeveloper.payments.application.PaymentsEventPublisher;

/*
 Zdarzenia aplikacyjne trafiają do Springa (i Event Publication Registry) w transakcji use case'u.
 Słuchacze (np. projekcja modelu odczytu) dostaną je dopiero po commicie.
 */
@Adapter
class SpringPaymentsEventPublisher implements PaymentsEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringPaymentsEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    SpringPaymentsEventPublisher(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(final PaymentsEvent event) {
        LOGGER.info("Publishing {}", event);
        eventPublisher.publishEvent(event);
    }

}
