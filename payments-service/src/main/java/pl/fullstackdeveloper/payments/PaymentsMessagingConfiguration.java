package pl.fullstackdeveloper.payments;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;
import pl.fullstackdeveloper.payments.api.PaymentProcessed;
import pl.fullstackdeveloper.payments.api.PaymentsTopics;

/*
 Eksternalizacja zdarzeń: PaymentProcessed opublikowane w transakcji trafia najpierw do outboxa
 (event_publication), a następnie do Kafki. Kluczem jest reference, więc komunikaty dotyczące
 jednego zamówienia trafiają do tej samej partycji (zachowana kolejność).
 */
@Configuration
class PaymentsMessagingConfiguration {

    @Bean
    EventExternalizationConfiguration eventExternalizationConfiguration() {
        return EventExternalizationConfiguration.externalizing()
                .selectByType(PaymentProcessed.class)
                .route(PaymentProcessed.class, event -> RoutingTarget.forTarget(PaymentsTopics.PAYMENT_PROCESSED).andKey(event.reference()))
                .build();
    }

}
