package pl.fullstackdeveloper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/*
 Demo transactional outbox (Event Publication Registry): zdarzenie zapisywane jest w tabeli
 event_publication w tej samej transakcji co zmiany biznesowe. Jeśli listener zawiedzie,
 publikacja nie znika - można ją ponowić (ręcznie, harmonogramem albo przy restarcie aplikacji).
 */
@SpringBootTest
class EventPublicationRegistryTest {

    record DemoEvent(String id) {
    }

    static class FlakyListener {

        private final AtomicInteger attempts = new AtomicInteger();
        private volatile boolean handled;

        @ApplicationModuleListener
        public void on(final DemoEvent event) {
            if (attempts.incrementAndGet() == 1) {
                throw new IllegalStateException("Temporary failure of " + event.id());
            }
            handled = true;
        }

        public int attempts() {
            return attempts.get();
        }

        public boolean handled() {
            return handled;
        }

    }

    @TestConfiguration
    static class Config {

        @Bean
        FlakyListener flakyListener() {
            return new FlakyListener();
        }

    }

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private FlakyListener listener;
    @Autowired
    private FailedEventPublications failedPublications;

    @Test
    void failed_publication_is_kept_and_can_be_resubmitted() {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(new DemoEvent("demo-1")));

        await().atMost(Duration.ofSeconds(5)).until(() -> listener.attempts() == 1);
        assertThat(listener.handled()).isFalse();

        failedPublications.resubmit(ResubmissionOptions.defaults());

        await().atMost(Duration.ofSeconds(5)).until(listener::handled);
        assertThat(listener.attempts()).isEqualTo(2);
    }

}
