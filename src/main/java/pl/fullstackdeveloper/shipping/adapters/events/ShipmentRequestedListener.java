package pl.fullstackdeveloper.shipping.adapters.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.shipping.api.ShipmentProcessed;
import pl.fullstackdeveloper.shipping.api.ShipmentRequested;
import pl.fullstackdeveloper.shipping.application.ScheduleShipmentUseCase;
import pl.fullstackdeveloper.shipping.domain.UndeliverableAddressException;

// Krok sagi po stronie wysyłek: komunikat -> use case -> odpowiedź
@Adapter
class ShipmentRequestedListener {

    private final ScheduleShipmentUseCase scheduleShipment;
    private final ApplicationEventPublisher eventPublisher;

    ShipmentRequestedListener(final ScheduleShipmentUseCase scheduleShipment, final ApplicationEventPublisher eventPublisher) {
        this.scheduleShipment = scheduleShipment;
        this.eventPublisher = eventPublisher;
    }

    @ApplicationModuleListener
    void on(final ShipmentRequested request) {
        try {
            var shipmentId = scheduleShipment.handle(request.reference(), request.address());
            eventPublisher.publishEvent(ShipmentProcessed.scheduled(request.reference(), shipmentId.value().toString()));
        } catch (UndeliverableAddressException exception) {
            eventPublisher.publishEvent(ShipmentProcessed.rejected(request.reference(), exception.getMessage()));
        }
    }

}
