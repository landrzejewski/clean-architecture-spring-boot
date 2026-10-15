package pl.fullstackdeveloper.shipping.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.shipping.domain.Address;
import pl.fullstackdeveloper.shipping.domain.Shipment;
import pl.fullstackdeveloper.shipping.domain.ShipmentId;
import pl.fullstackdeveloper.shipping.domain.UndeliverableAddressException;

/*
 Idempotentny krok sagi: jeśli wysyłka dla danej referencji już istnieje, zwracamy ją zamiast tworzyć drugą.
 */
@Atomic(noRollbackFor = UndeliverableAddressException.class)
public class ScheduleShipmentService implements ScheduleShipmentUseCase {

    private final ShipmentRepository shipmentRepository;

    public ScheduleShipmentService(final ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public ShipmentId handle(final String reference, final String address) {
        return shipmentRepository.findByReference(reference)
                .map(Shipment::getId)
                .orElseGet(() -> {
                    var shipment = Shipment.schedule(reference, new Address(address));
                    shipmentRepository.save(shipment);
                    return shipment.getId();
                });
    }

}
