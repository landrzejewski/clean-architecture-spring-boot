package pl.fullstackdeveloper.orders.adapters.events;

import org.springframework.modulith.events.ApplicationModuleListener;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.events.PaymentCompleted;
import pl.fullstackdeveloper.events.PaymentFailed;
import pl.fullstackdeveloper.events.PaymentRefunded;
import pl.fullstackdeveloper.events.ShipmentScheduled;
import pl.fullstackdeveloper.orders.application.OrderProgressService;
import pl.fullstackdeveloper.orders.domain.OrderId;

import java.util.UUID;

// Zamówienia słuchają faktów z płatności i wysyłek (ShipmentFailed ich nie dotyczy - reagują na nie płatności)
@Adapter
class ShopEventsListener {

    private final OrderProgressService orderProgress;

    ShopEventsListener(final OrderProgressService orderProgress) {
        this.orderProgress = orderProgress;
    }

    @ApplicationModuleListener
    void on(final PaymentCompleted event) {
        orderProgress.paid(orderId(event.orderId()), event.paymentId());
    }

    @ApplicationModuleListener
    void on(final PaymentFailed event) {
        orderProgress.paymentFailed(orderId(event.orderId()), event.reason());
    }

    @ApplicationModuleListener
    void on(final ShipmentScheduled event) {
        orderProgress.shipped(orderId(event.orderId()), event.shipmentId());
    }

    @ApplicationModuleListener
    void on(final PaymentRefunded event) {
        orderProgress.refunded(orderId(event.orderId()), event.reason());
    }

    private OrderId orderId(final String value) {
        return new OrderId(UUID.fromString(value));
    }

}
