package pl.fullstackdeveloper.orders.domain;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.fullstackdeveloper.orders.domain.SagaStep.*;

class OrderSagaTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private final OrderSaga saga = OrderSaga.start(new OrderId(), NOW);

    @Test
    void happy_path_completes_all_steps_in_order() {
        saga.stepSucceeded(CHARGE_PAYMENT, NOW);
        assertThat(saga.getCurrentStep()).isEqualTo(SCHEDULE_SHIPMENT);

        saga.stepSucceeded(SCHEDULE_SHIPMENT, NOW);

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPLETED);
        assertThat(saga.getCompletedSteps()).containsExactly(CHARGE_PAYMENT, SCHEDULE_SHIPMENT);
    }

    @Test
    void failure_of_first_step_needs_no_compensation() {
        saga.stepFailed(CHARGE_PAYMENT, "Insufficient balance", NOW);

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
        assertThat(saga.getCurrentStep()).isNull();
    }

    @Test
    void failure_of_later_step_compensates_completed_steps() {
        saga.stepSucceeded(CHARGE_PAYMENT, NOW);

        saga.stepFailed(SCHEDULE_SHIPMENT, "Undeliverable", NOW);

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATING);
        assertThat(saga.getCurrentStep()).isEqualTo(REFUND_PAYMENT);

        saga.stepSucceeded(REFUND_PAYMENT, NOW);

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.COMPENSATED);
        assertThat(saga.getFailureReason()).isEqualTo("Undeliverable");
    }

    @Test
    void failed_compensation_requires_manual_intervention() {
        saga.stepSucceeded(CHARGE_PAYMENT, NOW);
        saga.stepFailed(SCHEDULE_SHIPMENT, "Undeliverable", NOW);

        saga.stepFailed(REFUND_PAYMENT, "Card blocked", NOW);

        assertThat(saga.getStatus()).isEqualTo(SagaStatus.FAILED);
        assertThat(saga.getFailureReason()).contains("compensation failed");
    }

    @Test
    void saga_does_not_accept_results_it_does_not_wait_for() {
        assertThat(saga.awaits(SCHEDULE_SHIPMENT)).isFalse();
        assertThatThrownBy(() -> saga.stepSucceeded(SCHEDULE_SHIPMENT, NOW)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void step_without_response_times_out() {
        assertThat(saga.isTimedOut(NOW.plusSeconds(10), Duration.ofSeconds(30))).isFalse();
        assertThat(saga.isTimedOut(NOW.plusSeconds(31), Duration.ofSeconds(30))).isTrue();

        saga.retry(NOW.plusSeconds(31));

        assertThat(saga.getAttempts()).isEqualTo(1);
        assertThat(saga.isTimedOut(NOW.plusSeconds(40), Duration.ofSeconds(30))).isFalse();
    }

}
