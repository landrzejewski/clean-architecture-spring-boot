package pl.fullstackdeveloper.common.events;

import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

/*
 Automatyczne ponawianie publikacji, których listener zakończył się wyjątkiem (Event Publication Registry).
 Razem z idempotentnymi listenerami daje to dostarczanie "at-least-once" bez ręcznej interwencji.
 */
@Configuration
@EnableScheduling
class EventPublicationRetryScheduler {

    private final FailedEventPublications failedPublications;

    EventPublicationRetryScheduler(final FailedEventPublications failedPublications) {
        this.failedPublications = failedPublications;
    }

    @Scheduled(fixedDelayString = "${events.retry.interval:2s}")
    void resubmitFailed() {
        failedPublications.resubmit(ResubmissionOptions.defaults().withMinAge(Duration.ofSeconds(1)));
    }

}
