package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.orders.domain.OrderId;

@Atomic
public class CompletePaymentService implements CompletePaymentUseCase {

    private final OrderRepository orderRepository;

    public CompletePaymentService(final OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void handle(final OrderId orderId, final PaymentOutcome outcome) {
        var order = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
        switch (outcome) {
            case PaymentOutcome.Paid paid -> order.markPaid(paid.paymentId());
            case PaymentOutcome.Declined declined -> order.reject(declined.reason());
        }
        orderRepository.save(order);
    }

}
