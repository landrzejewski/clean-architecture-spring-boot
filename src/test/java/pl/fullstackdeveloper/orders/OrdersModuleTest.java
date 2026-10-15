package pl.fullstackdeveloper.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import pl.fullstackdeveloper.events.OrderPlaced;
import pl.fullstackdeveloper.events.PaymentCompleted;
import pl.fullstackdeveloper.events.PaymentRefunded;
import pl.fullstackdeveloper.events.ShipmentScheduled;
import pl.fullstackdeveloper.orders.application.GetOrderUseCase;
import pl.fullstackdeveloper.orders.application.PlaceOrderCommand;
import pl.fullstackdeveloper.orders.application.PlaceOrderUseCase;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.Price;

import static org.assertj.core.api.Assertions.assertThat;

/*
 Moduł zamówień w izolacji: płatności i wysyłki "udajemy", publikując ich zdarzenia (Scenario API).
 */
@ApplicationModuleTest(extraIncludes = {"common", "events"})
class OrdersModuleTest {

    @Autowired
    private PlaceOrderUseCase placeOrder;
    @Autowired
    private GetOrderUseCase getOrder;

    @Test
    void placing_order_announces_order_placed(final Scenario scenario) {
        scenario.stimulate(this::placeOrder)
                .andWaitForEventOfType(OrderPlaced.class)
                .toArriveAndVerify((event, orderId) -> assertThat(event.orderId()).isEqualTo(orderId.value().toString()));
    }

    @Test
    void order_follows_payment_and_shipment_facts(final Scenario scenario) {
        var orderId = placeOrder();
        var id = orderId.value().toString();

        scenario.publish(new PaymentCompleted(id, "payment-1"))
                .andWaitForStateChange(() -> status(orderId), "PAID"::equals);
        scenario.publish(new ShipmentScheduled(id, "shipment-1"))
                .andWaitForStateChange(() -> status(orderId), "CONFIRMED"::equals);
    }

    @Test
    void refund_cancels_paid_order(final Scenario scenario) {
        var orderId = placeOrder();
        var id = orderId.value().toString();

        scenario.publish(new PaymentCompleted(id, "payment-1"))
                .andWaitForStateChange(() -> status(orderId), "PAID"::equals);
        scenario.publish(new PaymentRefunded(id, "Address is outside of the delivery area"))
                .andWaitForStateChange(() -> status(orderId), "CANCELLED"::equals);
    }

    private OrderId placeOrder() {
        return placeOrder.handle(new PlaceOrderCommand("Pizza", new CardReference("1111222233334444"), Price.of("40", "PLN"), "Warszawa"));
    }

    private String status(final OrderId orderId) {
        return getOrder.handle(orderId).status();
    }

}
