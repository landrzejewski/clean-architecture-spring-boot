package pl.fullstackdeveloper.orders.adapters.shipping;

import org.springframework.context.ApplicationEventPublisher;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.orders.application.Shipping;
import pl.fullstackdeveloper.orders.domain.OrderId;
import pl.fullstackdeveloper.shipping.api.ShipmentRequested;

@Adapter
class AsyncShippingAdapter implements Shipping {

    private final ApplicationEventPublisher eventPublisher;

    AsyncShippingAdapter(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void requestShipment(final OrderId orderId, final String address) {
        eventPublisher.publishEvent(new ShipmentRequested(orderId.value().toString(), address));
    }

}
