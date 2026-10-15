package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.fullstackdeveloper.payments.api.PaymentRequest;
import pl.fullstackdeveloper.payments.api.PaymentResponse;
import pl.fullstackdeveloper.payments.api.PaymentsApi;

// PaymentsApi wystawione przez HTTP - ten sam kontrakt, który wcześniej był wywoływany w procesie
@RestController
class PaymentsRestController {

    private final PaymentsApi paymentsApi;

    PaymentsRestController(final PaymentsApi paymentsApi) {
        this.paymentsApi = paymentsApi;
    }

    @PostMapping("api/payments")
    PaymentResponse pay(@RequestBody final PaymentRequest request) {
        return PaymentResponse.from(paymentsApi.pay(request));
    }

}
