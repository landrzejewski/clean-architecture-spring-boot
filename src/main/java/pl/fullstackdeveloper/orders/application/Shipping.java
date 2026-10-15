package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.OrderId;

public interface Shipping {

    void requestShipment(OrderId orderId, String address);

}
