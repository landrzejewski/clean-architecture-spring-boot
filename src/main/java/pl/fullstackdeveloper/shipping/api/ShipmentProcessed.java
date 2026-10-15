package pl.fullstackdeveloper.shipping.api;

public record ShipmentProcessed(String reference, boolean scheduled, String shipmentId, String reason) {

    public static ShipmentProcessed scheduled(final String reference, final String shipmentId) {
        return new ShipmentProcessed(reference, true, shipmentId, null);
    }

    public static ShipmentProcessed rejected(final String reference, final String reason) {
        return new ShipmentProcessed(reference, false, null, reason);
    }

}
