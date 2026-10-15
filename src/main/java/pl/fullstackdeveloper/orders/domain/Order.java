package pl.fullstackdeveloper.orders.domain;

import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.Money;

/*
 UWAGA (krok 10 - wersja naiwna): model zamówień używa typów z domeny płatności (CardNumber, Money).
 Zmiana w payments.domain wymusza zmiany w orders - konteksty są ze sobą sklejone.
 */
public final class Order {

    private final OrderId id;
    private final String description;
    private final CardNumber cardNumber;
    private final Money total;
    private OrderStatus status = OrderStatus.NEW;
    private String paymentId;

    public Order(final OrderId id, final String description, final CardNumber cardNumber, final Money total) {
        this.id = id;
        this.description = description;
        this.cardNumber = cardNumber;
        this.total = total;
    }

    public void markPaid(final String paymentId) {
        requireStatus(OrderStatus.NEW);
        this.paymentId = paymentId;
        this.status = OrderStatus.PAID;
    }

    public void reject() {
        requireStatus(OrderStatus.NEW);
        this.status = OrderStatus.REJECTED;
    }

    private void requireStatus(final OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Order %s is %s, expected %s".formatted(id.value(), status, expected));
        }
    }

    public OrderId getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public CardNumber getCardNumber() {
        return cardNumber;
    }

    public Money getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentId() {
        return paymentId;
    }

}
