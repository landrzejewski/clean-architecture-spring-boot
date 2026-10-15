package pl.fullstackdeveloper.shipping.adapters.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "shipments")
class ShipmentEntity {

    @Id
    private String id;
    @Column(unique = true)
    private String reference;
    private String address;

    protected ShipmentEntity() {
    }

    ShipmentEntity(final String id, final String reference, final String address) {
        this.id = id;
        this.reference = reference;
        this.address = address;
    }

    String getId() {
        return id;
    }

    String getReference() {
        return reference;
    }

    String getAddress() {
        return address;
    }

}
