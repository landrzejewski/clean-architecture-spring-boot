package pl.fullstackdeveloper.events;

// Wysyłka zaplanowana - proces zakończony sukcesem
public record ShipmentScheduled(String orderId, String shipmentId) {
}
