package pl.fullstackdeveloper.orders.adapters.events;

import org.springframework.context.ApplicationEventPublisher;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.events.OrderPlaced;
import pl.fullstackdeveloper.orders.application.OrderEvents;
import pl.fullstackdeveloper.orders.domain.Order;

@Adapter
class SpringOrderEvents implements OrderEvents {

    private final ApplicationEventPublisher eventPublisher;

    SpringOrderEvents(final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void orderPlaced(final Order order) {
        eventPublisher.publishEvent(new OrderPlaced(order.getId().value().toString(), order.getCardReference().value(),
                order.getTotal().amount(), order.getTotal().currency().getCurrencyCode(), order.getAddress()));
    }

}
