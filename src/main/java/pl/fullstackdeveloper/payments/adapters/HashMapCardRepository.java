package pl.fullstackdeveloper.payments.adapters;

import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.ResultPage;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Adapter
final class HashMapCardRepository implements CardRepository {

    private final Map<CardNumber, Card> cards = new ConcurrentHashMap<>();

    @Override
    public Optional<Card> findByNumber(final CardNumber cardNumber) {
        return Optional.ofNullable(cards.get(cardNumber));
    }

    @Override
    public ResultPage<Card> findAll(final PageSpec pageSpec) {
        var allCards = new ArrayList<>(cards.values());
        var from = Math.min(pageSpec.index() * pageSpec.size(), allCards.size());
        var to = Math.min(from + pageSpec.size(), allCards.size());
        var totalPages = (int) Math.ceil((double) allCards.size() / pageSpec.size());
        return new ResultPage<>(allCards.subList(from, to), pageSpec, totalPages);
    }

    @Override
    public Card save(final Card card) {
        cards.put(card.getNumber(), card);
        return card;
    }

}
