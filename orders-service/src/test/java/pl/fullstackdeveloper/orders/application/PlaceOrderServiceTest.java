package pl.fullstackdeveloper.orders.application;

import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.orders.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

// Testy use case'ów zamówień z fake'ami portów - bez Springa i bez modułu płatności
class PlaceOrderServiceTest {

    private final Map<OrderId, Order> orders = new HashMap<>();
    private final List<OrderId> paymentRequests = new ArrayList<>();
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
    private final PlaceOrderUseCase placeOrder = new PlaceOrderService(orderRepository, (orderId, card, price) -> paymentRequests.add(orderId));
    private final CompletePaymentUseCase completePayment = new CompletePaymentService(orderRepository);

    @Test
    void placed_order_awaits_payment() {
        var orderId = placeOrder();

        assertThat(orders.get(orderId).getStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
        assertThat(paymentRequests).containsExactly(orderId);
    }

    @Test
    void order_is_paid_when_payment_completes() {
        var orderId = placeOrder();

        completePayment.handle(orderId, new PaymentOutcome.Paid("payment-1"));

        assertThat(orders.get(orderId).getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void order_is_rejected_when_payment_is_declined() {
        var orderId = placeOrder();

        completePayment.handle(orderId, new PaymentOutcome.Declined("Insufficient balance"));

        assertThat(orders.get(orderId).getRejectionReason()).isEqualTo("Insufficient balance");
    }

    private OrderId placeOrder() {
        return placeOrder.handle(new PlaceOrderCommand("Pizza", new CardReference("1111222233334444"), Price.of("40", "PLN")));
    }

}
