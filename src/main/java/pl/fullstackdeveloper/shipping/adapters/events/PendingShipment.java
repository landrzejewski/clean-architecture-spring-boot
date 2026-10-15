package pl.fullstackdeveloper.shipping.adapters.events;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Adres z OrderPlaced czeka, aż przyjdzie PaymentCompleted (kolejność zdarzeń nie jest gwarantowana)
@Entity
@Table(name = "pending_shipments")
class PendingShipment {

    @Id
    private String orderId;
    private String address;

    protected PendingShipment() {
    }

    PendingShipment(final String orderId, final String address) {
        this.orderId = orderId;
        this.address = address;
    }

    String getAddress() {
        return address;
    }

}
