package pl.fullstackdeveloper.shipping.adapters.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.modulith.events.ApplicationModuleListener;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.events.*;
import pl.fullstackdeveloper.shipping.application.ScheduleShipmentUseCase;
import pl.fullstackdeveloper.shipping.domain.UndeliverableAddressException;

interface PendingShipmentRepository extends JpaRepository<PendingShipment, String> {
}

/*
 Wysyłki w choreografii:
 - OrderPlaced      -> zapamiętaj adres
 - PaymentCompleted -> zaplanuj wysyłkę -> ShipmentScheduled / ShipmentFailed
 Jeśli PaymentCompleted dotrze przed OrderPlaced, listener rzuca wyjątek - publikacja zostaje
 oznaczona jako nieudana i jest ponawiana (EventPublicationRetryScheduler w common).
 */
@Adapter
class ShippingProcessListener {

    private final PendingShipmentRepository pendingShipments;
    private final ScheduleShipmentUseCase scheduleShipment;
    private final ApplicationEventPublisher eventPublisher;

    ShippingProcessListener(final PendingShipmentRepository pendingShipments, final ScheduleShipmentUseCase scheduleShipment,
                         final ApplicationEventPublisher eventPublisher) {
        this.pendingShipments = pendingShipments;
        this.scheduleShipment = scheduleShipment;
        this.eventPublisher = eventPublisher;
    }

    @ApplicationModuleListener
    void on(final OrderPlaced event) {
        pendingShipments.save(new PendingShipment(event.orderId(), event.address()));
    }

    @ApplicationModuleListener
    void on(final PaymentCompleted event) {
        var pending = pendingShipments.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException("Address for order %s not known yet".formatted(event.orderId())));
        try {
            var shipmentId = scheduleShipment.handle(event.orderId(), pending.getAddress());
            eventPublisher.publishEvent(new ShipmentScheduled(event.orderId(), shipmentId.value().toString()));
        } catch (UndeliverableAddressException exception) {
            eventPublisher.publishEvent(new ShipmentFailed(event.orderId(), exception.getMessage()));
        }
    }

}
