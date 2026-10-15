package pl.fullstackdeveloper.common.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.common.annotations.Retry;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AspectsTest {

    // Bean jest proxy (CGLIB) - stan odczytujemy przez metody, nie pola (pola proxy są puste)
    static class Operations {

        private final List<Boolean> transactionActive = new ArrayList<>();
        private int attempts;

        public List<Boolean> transactionActive() {
            return transactionActive;
        }

        public int attempts() {
            return attempts;
        }

        public void reset() {
            transactionActive.clear();
            attempts = 0;
        }

        @Atomic
        public void atomic() {
            transactionActive.add(TransactionSynchronizationManager.isActualTransactionActive());
        }

        public void plain() {
            transactionActive.add(TransactionSynchronizationManager.isActualTransactionActive());
        }

        // każda próba w osobnej transakcji, bo RetryAspect jest bardziej zewnętrzny niż AtomicAspect
        @Retry(attempts = 3)
        @Atomic
        public String flaky() {
            transactionActive.add(TransactionSynchronizationManager.isActualTransactionActive());
            if (++attempts < 3) {
                throw new IllegalStateException("temporary failure");
            }
            return "ok";
        }

        @Retry(attempts = 5, on = IllegalStateException.class)
        public void notRetryable() {
            attempts++;
            throw new IllegalArgumentException("permanent failure");
        }

    }

    @TestConfiguration
    static class Config {

        @Bean
        Operations operations() {
            return new Operations();
        }

    }

    @Autowired
    private Operations operations;

    @BeforeEach
    void beforeEach() {
        operations.reset();
    }

    @Test
    void atomic_method_runs_in_transaction() {
        operations.atomic();
        operations.plain();

        assertThat(operations.transactionActive()).containsExactly(true, false);
    }

    @Test
    void failed_attempts_are_retried() {
        assertThat(operations.flaky()).isEqualTo("ok");
        assertThat(operations.attempts()).isEqualTo(3);
        assertThat(operations.transactionActive()).containsOnly(true);
    }

    @Test
    void only_configured_exceptions_are_retried() {
        assertThatThrownBy(operations::notRetryable).isInstanceOf(IllegalArgumentException.class);
        assertThat(operations.attempts()).isEqualTo(1);
    }

}
