package pl.fullstackdeveloper.payments.api;

/*
 Publiczny kontrakt modułu płatności - jedyne, co mogą widzieć inne konteksty.
 Typy proste i własne rekordy API, żadnych typów z payments.domain / payments.application.
 */
public interface PaymentsApi {

    PaymentResult pay(PaymentRequest request);

    // zwrot = wpłata na kartę (kompensacja płatności)
    PaymentResult refund(PaymentRequest request);

}
