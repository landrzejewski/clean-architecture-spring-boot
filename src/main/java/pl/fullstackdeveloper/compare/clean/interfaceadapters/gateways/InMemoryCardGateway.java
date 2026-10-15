package pl.fullstackdeveloper.compare.clean.interfaceadapters.gateways;

import pl.fullstackdeveloper.compare.clean.entities.Card;
import pl.fullstackdeveloper.compare.clean.usecases.CardGateway;

import java.util.HashMap;
import java.util.Map;

public class InMemoryCardGateway implements CardGateway {

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
