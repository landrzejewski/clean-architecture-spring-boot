package pl.fullstackdeveloper.compare.onion.infrastructure.persistence;

import pl.fullstackdeveloper.compare.onion.domain.model.Card;
import pl.fullstackdeveloper.compare.onion.domain.services.CardRepository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryCardRepository implements CardRepository {

    private final Map<String, Card> cards = new HashMap<>();

    @Override
    public Card findByNumber(String number) {
        return cards.get(number);
    }

    @Override
    public void save(Card card) {
        cards.put(card.getNumber(), card);
    }

}
