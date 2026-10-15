package pl.fullstackdeveloper.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import pl.fullstackdeveloper.orders.application.GetOrderUseCase;
import pl.fullstackdeveloper.orders.application.PlaceOrderCommand;
import pl.fullstackdeveloper.orders.application.PlaceOrderUseCase;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.Price;
import pl.fullstackdeveloper.payments.api.PaymentProcessed;
import pl.fullstackdeveloper.payments.api.PaymentRequested;
import pl.fullstackdeveloper.payments.api.PaymentResult;

import static org.assertj.core.api.Assertions.assertThat;

/*
 Test modułu w izolacji: Spring uruchamia tylko beany modułu orders (+ common).
 Moduł płatności nie istnieje - interakcję z nim opisujemy zdarzeniami (Scenario API).
 */
@ApplicationModuleTest(extraIncludes = "common")
class OrdersModuleTest {

    @Autowired
    private PlaceOrderUseCase placeOrder;
    @Autowired
    private GetOrderUseCase getOrder;

    @Test
    void placing_order_requests_payment(final Scenario scenario) {
        scenario.stimulate(() -> placeOrder.handle(new PlaceOrderCommand("Pizza", new CardReference("1111222233334444"), Price.of("40", "PLN"))))
                .andWaitForEventOfType(PaymentRequested.class)
                .toArriveAndVerify((event, orderId) -> {
                    assertThat(event.reference()).isEqualTo(orderId.value().toString());
                    assertThat(event.amount()).isEqualByComparingTo("40");
                });
    }

    @Test
    void completed_payment_marks_order_as_paid(final Scenario scenario) {
        var orderId = placeOrder.handle(new PlaceOrderCommand("Book", new CardReference("1111222233334444"), Price.of("10", "PLN")));

        scenario.publish(new PaymentProcessed(orderId.value().toString(), new PaymentResult.Completed("payment-1")))
                .andWaitForStateChange(() -> status(orderId), "PAID"::equals)
                .andVerify(status -> assertThat(getOrder.handle(orderId).paymentId()).isEqualTo("payment-1"));
    }

    private String status(final OrderId orderId) {
        return getOrder.handle(orderId).status();
    }

}
