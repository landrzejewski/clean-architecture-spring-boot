package pl.fullstackdeveloper.compare.onion.infrastructure.web;

import pl.fullstackdeveloper.compare.onion.application.TopUpService;

import java.math.BigDecimal;

public class CardController {

    private final TopUpService topUpService;

    public CardController(TopUpService topUpService) {
        this.topUpService = topUpService;
    }

    public String topUp(String cardNumber, String amount) {
        return "Balance: " + topUpService.topUp(cardNumber, new BigDecimal(amount));
    }

}
