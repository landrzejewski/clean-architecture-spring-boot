package pl.fullstackdeveloper.orders.adapters.shipping;

import org.springframework.modulith.events.ApplicationModuleListener;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.OrderSagaOrchestrator;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.shipping.api.ShipmentProcessed;

import java.util.UUID;

@Adapter
class ShipmentProcessedListener {

    private final OrderSagaOrchestrator orchestrator;

    ShipmentProcessedListener(final OrderSagaOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @ApplicationModuleListener
    void on(final ShipmentProcessed event) {
        var orderId = new OrderId(UUID.fromString(event.reference()));
        if (event.scheduled()) {
            orchestrator.shipmentScheduled(orderId, event.shipmentId());
        } else {
            orchestrator.shipmentRejected(orderId, event.reason());
        }
    }

}
