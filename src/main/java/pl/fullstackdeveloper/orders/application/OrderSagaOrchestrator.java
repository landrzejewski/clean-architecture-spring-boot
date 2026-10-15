package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.OrderSaga;
import pl.fullstackdeveloper.orders.domain.OrderStatus;
import pl.fullstackdeveloper.orders.domain.SagaStatus;
import pl.fullstackdeveloper.orders.domain.SagaStep;

import java.time.Clock;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.logging.Logger;

/*
 Orkiestrator sagi (wariant orchestration): jedno miejsce, które zna cały proces i decyduje,
 jaki krok wykonać po sukcesie lub porażce poprzedniego. Uczestnicy (płatności, wysyłki) nie znają
 się nawzajem - odpowiadają tylko na polecenia.
 Każda reakcja: wczytaj stan -> zastosuj zdarzenie -> zapisz stan -> wyślij kolejne polecenie,
 w jednej transakcji (stan sagi i wychodzący komunikat trafiają do bazy razem - outbox).
 */
@Atomic
public class OrderSagaOrchestrator {

    private static final Logger LOGGER = Logger.getLogger(OrderSagaOrchestrator.class.getName());

    private final OrderRepository orderRepository;
    private final OrderSagaRepository sagaRepository;
    private final Payments payments;
    private final Shipping shipping;
    private final Clock clock;
    private final Duration stepTimeout;
    private final int maxAttempts;

    public OrderSagaOrchestrator(final OrderRepository orderRepository, final OrderSagaRepository sagaRepository, final Payments payments,
                                 final Shipping shipping, final Clock clock, final Duration stepTimeout, final int maxAttempts) {
        this.orderRepository = orderRepository;
        this.sagaRepository = sagaRepository;
        this.payments = payments;
        this.shipping = shipping;
        this.clock = clock;
        this.stepTimeout = stepTimeout;
        this.maxAttempts = maxAttempts;
    }

    public void start(final Order order) {
        var saga = OrderSaga.start(order.getId(), clock.instant());
        sagaRepository.save(saga);
        dispatch(saga, order);
    }

    public void paymentCompleted(final OrderId orderId, final String paymentId) {
        onSuccess(orderId, SagaStep.CHARGE_PAYMENT, order -> order.markPaid(paymentId));
    }

    public void paymentRejected(final OrderId orderId, final String reason) {
        onFailure(orderId, SagaStep.CHARGE_PAYMENT, reason);
    }

    public void shipmentScheduled(final OrderId orderId, final String shipmentId) {
        onSuccess(orderId, SagaStep.SCHEDULE_SHIPMENT, order -> order.confirm(shipmentId));
    }

    public void shipmentRejected(final OrderId orderId, final String reason) {
        onFailure(orderId, SagaStep.SCHEDULE_SHIPMENT, reason);
    }

    public void refundCompleted(final OrderId orderId) {
        onSuccess(orderId, SagaStep.REFUND_PAYMENT, order -> {
        });
    }

    public void refundRejected(final OrderId orderId, final String reason) {
        onFailure(orderId, SagaStep.REFUND_PAYMENT, reason);
    }

    // Wywoływane cyklicznie: kroki bez odpowiedzi ponawiamy (są idempotentne), po limicie prób - porażka kroku
    public void handleTimeouts() {
        var now = clock.instant();
        for (var saga : sagaRepository.findActive()) {
            if (!saga.isTimedOut(now, stepTimeout)) {
                continue;
            }
            var order = findOrder(saga.getOrderId());
            if (saga.getAttempts() + 1 >= maxAttempts) {
                LOGGER.warning("Saga %s: step %s timed out".formatted(saga.getOrderId().value(), saga.getCurrentStep()));
                failStep(saga, order, "Timeout of step " + saga.getCurrentStep());
                continue;
            }
            LOGGER.info("Saga %s: retrying step %s".formatted(saga.getOrderId().value(), saga.getCurrentStep()));
            saga.retry(now);
            sagaRepository.save(saga);
            dispatch(saga, order);
        }
    }

    private void onSuccess(final OrderId orderId, final SagaStep step, final Consumer<Order> orderChange) {
        var saga = findSaga(orderId);
        if (!saga.awaits(step)) {
            LOGGER.info("Saga %s: ignoring duplicate or late result of %s".formatted(orderId.value(), step));
            return;
        }
        var order = findOrder(orderId);
        orderChange.accept(order);
        saga.stepSucceeded(step, clock.instant());
        finishIfCompensated(saga, order);
        orderRepository.save(order);
        sagaRepository.save(saga);
        dispatch(saga, order);
    }

    private void onFailure(final OrderId orderId, final SagaStep step, final String reason) {
        var saga = findSaga(orderId);
        if (!saga.awaits(step)) {
            LOGGER.info("Saga %s: ignoring duplicate or late failure of %s".formatted(orderId.value(), step));
            return;
        }
        failStep(saga, findOrder(orderId), reason);
    }

    private void failStep(final OrderSaga saga, final Order order, final String reason) {
        // porażka pierwszego kroku: nie ma czego kompensować, zamówienie zostaje odrzucone
        if (saga.getCurrentStep() == SagaStep.CHARGE_PAYMENT) {
            order.reject(reason);
        }
        saga.stepFailed(saga.getCurrentStep(), reason, clock.instant());
        finishIfCompensated(saga, order);
        orderRepository.save(order);
        sagaRepository.save(saga);
        dispatch(saga, order);
    }

    private void finishIfCompensated(final OrderSaga saga, final Order order) {
        if (saga.getStatus() == SagaStatus.COMPENSATED && order.getStatus() == OrderStatus.PAID) {
            order.cancel(saga.getFailureReason());
        }
    }

    // Polecenie dla bieżącego kroku - ponowne wysłanie jest bezpieczne, bo uczestnicy są idempotentni
    private void dispatch(final OrderSaga saga, final Order order) {
        if (saga.getCurrentStep() == null) {
            LOGGER.info("Saga %s finished: %s".formatted(saga.getOrderId().value(), saga.getStatus()));
            return;
        }
        switch (saga.getCurrentStep()) {
            case CHARGE_PAYMENT -> payments.requestPayment(order.getId(), order.getCardReference(), order.getTotal());
            case SCHEDULE_SHIPMENT -> shipping.requestShipment(order.getId(), order.getAddress());
            case REFUND_PAYMENT -> payments.requestRefund(order.getId(), order.getCardReference(), order.getTotal());
        }
    }

    private OrderSaga findSaga(final OrderId orderId) {
        return sagaRepository.findById(orderId).orElseThrow(() -> new IllegalStateException("No saga for order " + orderId.value()));
    }

    private Order findOrder(final OrderId orderId) {
        return orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
    }

}
