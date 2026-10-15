package pl.fullstackdeveloper.orders.adapters.persistence;

import jakarta.persistence.*;
import pl.fullstackdeveloper.orders.domain.OrderStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
class OrderEntity {

    @Id
    private String id;
    private String description;
    private String cardReference;
    private BigDecimal amount;
    private String currencyCode;
    private String address;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String paymentId;
    private String shipmentId;
    private String rejectionReason;
    @Version
    private long version;

    protected OrderEntity() {
    }

    OrderEntity(final String id) {
        this.id = id;
    }

    String getId() {
        return id;
    }

    String getDescription() {
        return description;
    }

    void setDescription(final String description) {
        this.description = description;
    }

    String getCardReference() {
        return cardReference;
    }

    void setCardReference(final String cardReference) {
        this.cardReference = cardReference;
    }

    BigDecimal getAmount() {
        return amount;
    }

    void setAmount(final BigDecimal amount) {
        this.amount = amount;
    }

    String getCurrencyCode() {
        return currencyCode;
    }

    void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

    String getAddress() {
        return address;
    }

    void setAddress(final String address) {
        this.address = address;
    }

    String getShipmentId() {
        return shipmentId;
    }

    void setShipmentId(final String shipmentId) {
        this.shipmentId = shipmentId;
    }

    OrderStatus getStatus() {
        return status;
    }

    void setStatus(final OrderStatus status) {
        this.status = status;
    }

    String getPaymentId() {
        return paymentId;
    }

    void setPaymentId(final String paymentId) {
        this.paymentId = paymentId;
    }

    String getRejectionReason() {
        return rejectionReason;
    }

    void setRejectionReason(final String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

}
