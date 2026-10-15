package pl.fullstackdeveloper.orders.adapters.scheduling;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import pl.fullstackdeveloper.orders.application.OrderSagaOrchestrator;

// Adapter wejściowy "zegar": regularnie budzi orkiestrator, który ponawia lub kończy zawieszone kroki
@Configuration
@EnableScheduling
class SagaTimeoutScheduler {

    private final OrderSagaOrchestrator orchestrator;

    SagaTimeoutScheduler(final OrderSagaOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Scheduled(fixedDelayString = "${orders.saga.check-interval:5s}")
    void checkTimeouts() {
        orchestrator.handleTimeouts();
    }

}
