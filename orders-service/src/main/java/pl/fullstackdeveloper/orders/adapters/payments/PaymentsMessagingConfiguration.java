package pl.fullstackdeveloper.orders.adapters.payments;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;
import org.springframework.resilience.annotation.EnableResilientMethods;
import pl.fullstackdeveloper.payments.api.PaymentRequested;
import pl.fullstackdeveloper.payments.api.PaymentsTopics;

@Configuration
@EnableResilientMethods
class PaymentsMessagingConfiguration {

    // PaymentRequested publikowane przez AsyncPaymentsAdapter (bez zmian od 14) trafia przez outbox do Kafki
    @Bean
    EventExternalizationConfiguration eventExternalizationConfiguration() {
        return EventExternalizationConfiguration.externalizing()
                .selectByType(PaymentRequested.class)
                .route(PaymentRequested.class, event -> RoutingTarget.forTarget(PaymentsTopics.PAYMENT_REQUESTED).andKey(event.reference()))
                .build();
    }

}
