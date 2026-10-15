package pl.fullstackdeveloper.compare.hexagonal.adapter.in.web;

import pl.fullstackdeveloper.compare.hexagonal.application.port.in.TopUpCommand;
import pl.fullstackdeveloper.compare.hexagonal.application.port.in.TopUpUseCase;

import java.math.BigDecimal;

// Adapter wejściowy zna tylko port wejściowy, nie implementację
public class CardController {

    private final TopUpUseCase topUpUseCase;

    public CardController(TopUpUseCase topUpUseCase) {
        this.topUpUseCase = topUpUseCase;
    }

    public String topUp(String cardNumber, String amount) {
        return "Balance: " + topUpUseCase.topUp(new TopUpCommand(cardNumber, new BigDecimal(amount)));
    }

}
