package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.ResultPage;
import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.Optional;

// Port wyjściowy (driven port) - aplikacja definiuje, czego potrzebuje; implementację dostarcza adapter
public interface CardRepository {

    Optional<Card> findByNumber(CardNumber cardNumber);

    ResultPage<Card> findAll(PageSpec pageSpec);

    Card save(Card card);

}
