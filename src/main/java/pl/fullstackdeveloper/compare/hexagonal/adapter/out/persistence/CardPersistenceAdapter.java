package pl.fullstackdeveloper.compare.hexagonal.adapter.out.persistence;

import pl.fullstackdeveloper.compare.hexagonal.application.port.out.LoadCardPort;
import pl.fullstackdeveloper.compare.hexagonal.application.port.out.SaveCardPort;
import pl.fullstackdeveloper.compare.hexagonal.domain.Card;

import java.util.HashMap;
import java.util.Map;

public class CardPersistenceAdapter implements LoadCardPort, SaveCardPort {

    private final Map<String, Card> cards = new HashMap<>();

    @Override
    public Card load(String cardNumber) {
        return cards.get(cardNumber);
    }

    @Override
    public void save(Card card) {
        cards.put(card.getNumber(), card);
    }

}
