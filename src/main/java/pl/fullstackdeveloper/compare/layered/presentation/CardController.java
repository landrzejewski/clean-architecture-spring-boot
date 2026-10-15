package pl.fullstackdeveloper.compare.layered.presentation;

import pl.fullstackdeveloper.compare.layered.business.CardService;

import java.math.BigDecimal;

public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    public String topUp(String cardNumber, String amount) {
        var balance = cardService.topUp(cardNumber, new BigDecimal(amount));
        return "Balance: " + balance;
    }

}
