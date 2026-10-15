package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.Order;

import java.math.BigDecimal;

public record OrderView(String id, String description, String cardNumber, BigDecimal total, String currencyCode,
                        String status, String paymentId, String rejectionReason) {

    static OrderView from(final Order order) {
        return new OrderView(order.getId().value().toString(), order.getDescription(), order.getCardReference().value(),
                order.getTotal().amount(), order.getTotal().currency().getCurrencyCode(), order.getStatus().name(), order.getPaymentId(),
                order.getRejectionReason());
    }

}
