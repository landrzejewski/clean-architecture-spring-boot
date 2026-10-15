package pl.fullstackdeveloper.payments.adapters.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.modulith.events.ApplicationModuleListener;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.events.*;
import pl.fullstackdeveloper.payments.api.PaymentRequest;
import pl.fullstackdeveloper.payments.api.PaymentResult;
import pl.fullstackdeveloper.payments.api.PaymentsApi;

interface OrderPaymentRepository extends JpaRepository<OrderPayment, String> {
}

/*
 Płatności w choreografii: same decydują, na co reagują.
 - OrderPlaced    -> obciąż kartę -> PaymentCompleted / PaymentFailed
 - ShipmentFailed -> zwróć środki (kompensacja) -> PaymentRefunded
 Obie reakcje są idempotentne (ProcessedMessages).
 */
@Adapter
class PaymentsProcessListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsProcessListener.class);

    private final PaymentsApi paymentsApi;
    private final ProcessedMessages processedMessages;
    private final OrderPaymentRepository orderPayments;
    private final ApplicationEventPublisher eventPublisher;

    PaymentsProcessListener(final PaymentsApi paymentsApi, final ProcessedMessages processedMessages,
                         final OrderPaymentRepository orderPayments, final ApplicationEventPublisher eventPublisher) {
        this.paymentsApi = paymentsApi;
        this.processedMessages = processedMessages;
        this.orderPayments = orderPayments;
        this.eventPublisher = eventPublisher;
    }

    @ApplicationModuleListener
    void on(final OrderPlaced event) {
        LOGGER.info("Charging card for order {}", event.orderId());
        orderPayments.save(new OrderPayment(event.orderId(), event.cardNumber(), event.amount(), event.currencyCode()));
        var result = processedMessages.processOnce("payment:" + event.orderId(), () ->
                paymentsApi.pay(new PaymentRequest(event.cardNumber(), event.amount(), event.currencyCode())));
        switch (result) {
            case PaymentResult.Completed completed -> eventPublisher.publishEvent(new PaymentCompleted(event.orderId(), completed.paymentId()));
            case PaymentResult.Rejected rejected -> eventPublisher.publishEvent(new PaymentFailed(event.orderId(), rejected.reason()));
        }
    }

    @ApplicationModuleListener
    void on(final ShipmentFailed event) {
        LOGGER.info("Refunding order {}: {}", event.orderId(), event.reason());
        var payment = orderPayments.findById(event.orderId()).orElseThrow();
        var result = processedMessages.processOnce("refund:" + event.orderId(), () ->
                paymentsApi.refund(new PaymentRequest(payment.getCardNumber(), payment.getAmount(), payment.getCurrencyCode())));
        if (result instanceof PaymentResult.Completed) {
            eventPublisher.publishEvent(new PaymentRefunded(event.orderId(), event.reason()));
        } else {
            // kompensacja nieudana - w choreografii nie ma jednego miejsca, które to zauważy (potrzebny monitoring)
            LOGGER.error("Refund for order {} failed: {}", event.orderId(), result);
        }
    }

}
