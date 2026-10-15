package pl.fullstackdeveloper.orders.application;

import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.Order;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.orders.domain.OrderStatus;
import pl.fullstackdeveloper.orders.domain.Price;
import pl.fullstackdeveloper.payments.api.PaymentResult;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

// Moduł zamówień testujemy z fake'iem PaymentsApi - implementacji płatności nie ma nawet na classpath
class PlaceOrderServiceTest {

    private final Map<OrderId, Order> orders = new HashMap<>();
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

    @Test
    void order_is_paid_when_payment_completes() {
        var service = new PlaceOrderService(orderRepository, request -> new PaymentResult.Completed("payment-1"));

        var orderId = service.handle(new PlaceOrderCommand("Pizza", new CardReference("1111222233334444"), Price.of("40", "PLN")));

        assertThat(orders.get(orderId).getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void order_is_rejected_when_payment_is_rejected() {
        var service = new PlaceOrderService(orderRepository, request -> new PaymentResult.Rejected("Insufficient balance"));

        var orderId = service.handle(new PlaceOrderCommand("Pizza", new CardReference("1111222233334444"), Price.of("40", "PLN")));

        assertThat(orders.get(orderId).getRejectionReason()).isEqualTo("Insufficient balance");
    }

}
