package pl.fullstackdeveloper.compare.onion.application;

import pl.fullstackdeveloper.compare.onion.domain.services.CardRepository;

import java.math.BigDecimal;

public class TopUpService {

    private final CardRepository cardRepository;

    public TopUpService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public BigDecimal topUp(String cardNumber, BigDecimal amount) {
        var card = cardRepository.findByNumber(cardNumber);
        card.topUp(amount);
        cardRepository.save(card);
        return card.getBalance();
    }

}
