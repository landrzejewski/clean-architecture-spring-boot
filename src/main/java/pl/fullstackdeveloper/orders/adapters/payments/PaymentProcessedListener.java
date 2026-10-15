package pl.fullstackdeveloper.orders.adapters.payments;

import org.springframework.modulith.events.ApplicationModuleListener;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.CompletePaymentUseCase;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.payments.api.PaymentProcessed;

import java.util.UUID;

// Adapter wejściowy zamówień: zdarzenie z płatności -> use case zamówień
@Adapter
class PaymentProcessedListener {

    private final CompletePaymentUseCase completePayment;

    PaymentProcessedListener(final CompletePaymentUseCase completePayment) {
        this.completePayment = completePayment;
    }

    @ApplicationModuleListener
    void on(final PaymentProcessed event) {
        var orderId = new OrderId(UUID.fromString(event.reference()));
        completePayment.handle(orderId, PaymentsTranslator.toOutcome(event.result()));
    }

}
