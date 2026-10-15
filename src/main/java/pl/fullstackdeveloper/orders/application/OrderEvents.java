package pl.fullstackdeveloper.orders.application;

import pl.fullstackdeveloper.orders.domain.Order;

// Port wyjściowy: ogłoszenie faktu "zamówienie złożone" - zamówienia nie wiedzą, kto na niego zareaguje
public interface OrderEvents {

    void orderPlaced(Order order);

}
