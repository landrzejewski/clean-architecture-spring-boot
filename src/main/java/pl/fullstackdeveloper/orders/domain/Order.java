package pl.fullstackdeveloper.orders.domain;

public final class Order {

    private final OrderId id;
    private final String description;
    private final CardReference cardReference;
    private final Price total;
    private final String address;
    private OrderStatus status = OrderStatus.AWAITING_PAYMENT;
    private String paymentId;
    private String shipmentId;
    private String rejectionReason;

    public Order(final OrderId id, final String description, final CardReference cardReference, final Price total, final String address) {
        this.id = id;
        this.description = description;
        this.cardReference = cardReference;
        this.total = total;
        this.address = address;
    }

    // Odtworzenie zamówienia z bazy (adapter persystencji) - bez walidacji przejść stanów
    public static Order restore(final OrderId id, final String description, final CardReference cardReference, final Price total,
                                final String address, final OrderStatus status, final String paymentId, final String shipmentId,
                                final String rejectionReason) {
        var order = new Order(id, description, cardReference, total, address);
        order.status = status;
        order.paymentId = paymentId;
        order.shipmentId = shipmentId;
        order.rejectionReason = rejectionReason;
        return order;
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

    public void confirm(final String shipmentId) {
        requireStatus(OrderStatus.PAID);
        this.shipmentId = shipmentId;
        this.status = OrderStatus.CONFIRMED;
    }

    // Anulowanie opłaconego zamówienia - stan końcowy po kompensacji sagi
    public void cancel(final String reason) {
        requireStatus(OrderStatus.PAID);
        this.rejectionReason = reason;
        this.status = OrderStatus.CANCELLED;
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

    public String getAddress() {
        return address;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

}
