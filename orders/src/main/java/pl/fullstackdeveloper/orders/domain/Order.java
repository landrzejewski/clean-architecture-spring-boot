package pl.fullstackdeveloper.orders.domain;

public final class Order {

    private final OrderId id;
    private final String description;
    private final CardReference cardReference;
    private final Price total;
    private OrderStatus status = OrderStatus.AWAITING_PAYMENT;
    private String paymentId;
    private String rejectionReason;

    public Order(final OrderId id, final String description, final CardReference cardReference, final Price total) {
        this.id = id;
        this.description = description;
        this.cardReference = cardReference;
        this.total = total;
    }

    public void markPaid(final String paymentId) {
        requireStatus(OrderStatus.AWAITING_PAYMENT);
        this.paymentId = paymentId;
        this.status = OrderStatus.PAID;
    }

    public void reject(final String reason) {
        requireStatus(OrderStatus.AWAITING_PAYMENT);
        this.rejectionReason = reason;
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

    public CardReference getCardReference() {
        return cardReference;
    }

    public Price getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

}
