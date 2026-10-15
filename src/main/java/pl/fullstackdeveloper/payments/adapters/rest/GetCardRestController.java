package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pl.fullstackdeveloper.payments.application.GetCardUseCase;
import pl.fullstackdeveloper.payments.domain.CardNumber;

@RestController
class GetCardRestController {

    private final GetCardUseCase getCardUseCase;
    private final CardRestMapper mapper;

    GetCardRestController(final GetCardUseCase getCardUseCase, final CardRestMapper mapper) {
        this.getCardUseCase = getCardUseCase;
        this.mapper = mapper;
    }

    @GetMapping("api/cards/{number:\\d{16,19}}")
    CardResponse getCard(@PathVariable final String number) {
        var cardView = getCardUseCase.handle(new CardNumber(number));
        return mapper.toResponse(cardView);
    }

}
