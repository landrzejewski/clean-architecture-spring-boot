package pl.fullstackdeveloper.orders.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private final Order order = new Order(new OrderId(), "Pizza", new CardReference("1111222233334444"), Price.of("40", "PLN"), "Warszawa, Polna 1");

    @Test
    void new_order_awaits_payment() {
        assertThat(order.getStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
    }

    @Test
    void order_can_be_paid() {
        order.markPaid("payment-1");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentId()).isEqualTo("payment-1");
    }

    @Test
    void paid_order_cannot_be_rejected() {
        order.markPaid("payment-1");

        assertThatThrownBy(() -> order.reject("too late")).isInstanceOf(IllegalStateException.class);
    }

}
