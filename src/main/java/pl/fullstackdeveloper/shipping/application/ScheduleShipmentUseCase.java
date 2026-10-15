package pl.fullstackdeveloper.shipping.application;

import pl.fullstackdeveloper.shipping.domain.ShipmentId;

public interface ScheduleShipmentUseCase {

    ShipmentId handle(String reference, String address);

}
