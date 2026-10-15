package pl.fullstackdeveloper.orders.application;

import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.orders.domain.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/*
 Orkiestrator testowany bez Springa: porty zastąpione fake'ami, które zapisują wysłane polecenia.
 Widać kolejność kroków i kompensacji.
 */
class OrderSagaOrchestratorTest {

    private final Map<OrderId, Order> orders = new HashMap<>();
    private final Map<OrderId, OrderSaga> sagas = new HashMap<>();
    private final List<String> commands = new ArrayList<>();
    private Instant now = Instant.parse("2026-01-01T12:00:00Z");

    private final OrderRepository orderRepository = new OrderRepository() {

        @Override
        public Optional<Order> findById(final OrderId id) {
            return Optional.ofNullable(orders.get(id));
        }

        @Override
        public List<Order> findByCardReference(final CardReference cardReference) {
            return List.of();
        }

        @Override
        public Order save(final Order order) {
            orders.put(order.getId(), order);
            return order;
        }

    };
    private final OrderSagaRepository sagaRepository = new OrderSagaRepository() {

        @Override
        public Optional<OrderSaga> findById(final OrderId orderId) {
            return Optional.ofNullable(sagas.get(orderId));
        }

        @Override
        public List<OrderSaga> findActive() {
            return sagas.values().stream().filter(saga -> saga.getStatus().isActive()).toList();
        }

        @Override
        public void save(final OrderSaga saga) {
            sagas.put(saga.getOrderId(), saga);
        }

    };
    private final Payments payments = new Payments() {

        @Override
        public void requestPayment(final OrderId orderId, final CardReference card, final Price price) {
            commands.add("pay");
        }

        @Override
        public void requestRefund(final OrderId orderId, final CardReference card, final Price price) {
            commands.add("refund");
        }

    };
    private final Shipping shipping = (orderId, address) -> commands.add("ship");
    private final Clock clock = new Clock() {

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }

    };
    private final OrderSagaOrchestrator orchestrator = new OrderSagaOrchestrator(orderRepository, sagaRepository, payments, shipping,
            clock, Duration.ofSeconds(30), 3);
    private final PlaceOrderUseCase placeOrder = new PlaceOrderService(orderRepository, orchestrator);

    @Test
    void successful_saga_confirms_order() {
        var orderId = placeOrder("Warszawa");

        orchestrator.paymentCompleted(orderId, "payment-1");
        orchestrator.shipmentScheduled(orderId, "shipment-1");

        assertThat(commands).containsExactly("pay", "ship");
        assertThat(orders.get(orderId).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(sagas.get(orderId).getStatus()).isEqualTo(SagaStatus.COMPLETED);
    }

    @Test
    void rejected_shipment_triggers_refund_and_cancels_order() {
        var orderId = placeOrder("Antarctica");

        orchestrator.paymentCompleted(orderId, "payment-1");
        orchestrator.shipmentRejected(orderId, "Address is outside of the delivery area");
        orchestrator.refundCompleted(orderId);

        assertThat(commands).containsExactly("pay", "ship", "refund");
        assertThat(orders.get(orderId).getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(sagas.get(orderId).getStatus()).isEqualTo(SagaStatus.COMPENSATED);
    }

    @Test
    void rejected_payment_rejects_order() {
        var orderId = placeOrder("Warszawa");

        orchestrator.paymentRejected(orderId, "Insufficient balance");

        assertThat(commands).containsExactly("pay");
        assertThat(orders.get(orderId).getStatus()).isEqualTo(OrderStatus.REJECTED);
    }

    @Test
    void duplicate_results_are_ignored() {
        var orderId = placeOrder("Warszawa");

        orchestrator.paymentCompleted(orderId, "payment-1");
        orchestrator.paymentCompleted(orderId, "payment-1");

        assertThat(commands).containsExactly("pay", "ship");
    }

    @Test
    void step_without_response_is_retried_and_then_compensated() {
        var orderId = placeOrder("Warszawa");
        orchestrator.paymentCompleted(orderId, "payment-1");

        now = now.plusSeconds(31);
        orchestrator.handleTimeouts();
        now = now.plusSeconds(31);
        orchestrator.handleTimeouts();
        now = now.plusSeconds(31);
        orchestrator.handleTimeouts();

        assertThat(commands).containsExactly("pay", "ship", "ship", "ship", "refund");
        assertThat(sagas.get(orderId).getStatus()).isEqualTo(SagaStatus.COMPENSATING);
    }

    private OrderId placeOrder(final String address) {
        return placeOrder.handle(new PlaceOrderCommand("Pizza", new CardReference("1111222233334444"), Price.of("40", "PLN"), address));
    }

}
