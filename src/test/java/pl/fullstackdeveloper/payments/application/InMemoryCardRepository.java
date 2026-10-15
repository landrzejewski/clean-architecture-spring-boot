package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Fake portu wyjściowego - test warstwy aplikacji nie zależy od adapterów
final class InMemoryCardRepository implements CardRepository {

    private final Map<CardNumber, Card> cards = new HashMap<>();

    @Override
    public Optional<Card> findByNumber(final CardNumber cardNumber) {
        return Optional.ofNullable(cards.get(cardNumber));
    }

    @Override
    public Card save(final Card card) {
        cards.put(card.getNumber(), card);
        return card;
    }

}
