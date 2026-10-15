package pl.fullstackdeveloper.orders.adapters.payments;

import pl.fullstackdeveloper.orders.application.PaymentOutcome;
import pl.fullstackdeveloper.payments.api.PaymentResult;

// Anti-Corruption Layer: kontrakt płatności -> język zamówień. Jedyne miejsce, które zna oba modele.
final class PaymentsTranslator {

    static PaymentOutcome toOutcome(final PaymentResult result) {
        return switch (result) {
            case PaymentResult.Completed completed -> new PaymentOutcome.Paid(completed.paymentId());
            case PaymentResult.Rejected rejected -> new PaymentOutcome.Declined(rejected.reason());
        };
    }

    private PaymentsTranslator() {
    }

}
