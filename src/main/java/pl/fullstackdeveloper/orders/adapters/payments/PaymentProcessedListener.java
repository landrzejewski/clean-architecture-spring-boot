package pl.fullstackdeveloper.orders.adapters.payments;

import org.springframework.modulith.events.ApplicationModuleListener;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.OrderSagaOrchestrator;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.payments.api.PaymentProcessed;
import pl.fullstackdeveloper.payments.api.PaymentResult;
import pl.fullstackdeveloper.payments.api.RefundProcessed;

import java.util.UUID;

// Odpowiedzi płatności -> orkiestrator (ACL: PaymentResult -> wywołania w języku sagi)
@Adapter
class PaymentProcessedListener {

    private final OrderSagaOrchestrator orchestrator;

    PaymentProcessedListener(final OrderSagaOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @ApplicationModuleListener
    void on(final PaymentProcessed event) {
        var orderId = new OrderId(UUID.fromString(event.reference()));
        switch (event.result()) {
            case PaymentResult.Completed completed -> orchestrator.paymentCompleted(orderId, completed.paymentId());
            case PaymentResult.Rejected rejected -> orchestrator.paymentRejected(orderId, rejected.reason());
        }
    }

    @ApplicationModuleListener
    void on(final RefundProcessed event) {
        var orderId = new OrderId(UUID.fromString(event.reference()));
        switch (event.result()) {
            case PaymentResult.Completed completed -> orchestrator.refundCompleted(orderId);
            case PaymentResult.Rejected rejected -> orchestrator.refundRejected(orderId, rejected.reason());
        }
    }

}
