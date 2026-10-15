package pl.fullstackdeveloper.shipping.adapters.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.shipping.application.ShipmentRepository;
import pl.fullstackdeveloper.shipping.domain.Address;
import pl.fullstackdeveloper.shipping.domain.Shipment;
import pl.fullstackdeveloper.shipping.domain.ShipmentId;

import java.util.Optional;
import java.util.UUID;

interface SpringDataShipmentRepository extends JpaRepository<ShipmentEntity, String> {

    Optional<ShipmentEntity> findByReference(String reference);

}

@Adapter
class JpaShipmentRepository implements ShipmentRepository {

    private final SpringDataShipmentRepository repository;

    JpaShipmentRepository(final SpringDataShipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Shipment> findByReference(final String reference) {
        return repository.findByReference(reference)
                .map(entity -> Shipment.restore(new ShipmentId(UUID.fromString(entity.getId())), entity.getReference(),
                        new Address(entity.getAddress())));
    }

    @Override
    public void save(final Shipment shipment) {
        repository.save(new ShipmentEntity(shipment.getId().value().toString(), shipment.getReference(), shipment.getAddress().value()));
    }

}
