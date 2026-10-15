package pl.fullstackdeveloper.shipping.domain;

public final class Shipment {

    private final ShipmentId id;
    private final String reference;
    private final Address address;

    private Shipment(final ShipmentId id, final String reference, final Address address) {
        this.id = id;
        this.reference = reference;
        this.address = address;
    }

    public static Shipment schedule(final String reference, final Address address) {
        if (!address.isDeliverable()) {
            throw new UndeliverableAddressException();
        }
        return new Shipment(new ShipmentId(), reference, address);
    }

    public static Shipment restore(final ShipmentId id, final String reference, final Address address) {
        return new Shipment(id, reference, address);
    }

    public ShipmentId getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public Address getAddress() {
        return address;
    }

}
