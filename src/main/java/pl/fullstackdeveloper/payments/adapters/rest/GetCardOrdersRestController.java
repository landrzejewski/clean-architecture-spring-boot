package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.fullstackdeveloper.orders.application.GetCardOrdersUseCase;
import pl.fullstackdeveloper.orders.application.OrderView;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.List;

/*
 UWAGA (krok 10 - wersja naiwna): "przy okazji" płatności pokazują zamówienia opłacone kartą.
 Teraz payments zależy od orders, a orders od payments - powstał cykl między kontekstami.
 */
@RestController
class GetCardOrdersRestController {

    private final GetCardOrdersUseCase getCardOrdersUseCase;

    GetCardOrdersRestController(final GetCardOrdersUseCase getCardOrdersUseCase) {
        this.getCardOrdersUseCase = getCardOrdersUseCase;
    }

    @GetMapping("api/cards/{number:\\d{16,19}}/orders")
    List<OrderView> getCardOrders(@PathVariable final String number) {
        return getCardOrdersUseCase.handle(new CardNumber(number));
    }

}
