package pl.fullstackdeveloper.shipping.application;

import pl.fullstackdeveloper.shipping.domain.Shipment;

import java.util.Optional;

public interface ShipmentRepository {

    Optional<Shipment> findByReference(String reference);

    void save(Shipment shipment);

}
