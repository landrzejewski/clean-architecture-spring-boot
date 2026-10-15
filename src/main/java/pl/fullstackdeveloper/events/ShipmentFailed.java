package pl.fullstackdeveloper.events;

// Wysyłka niemożliwa - płatności reagują zwrotem (kompensacja)
public record ShipmentFailed(String orderId, String reason) {
}
