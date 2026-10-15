package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.util.Optional;

// Port wyjściowy strony zapisu - od 17 (CQRS) bez metod do przeglądania danych: odczyty obsługuje strona zapytań
public interface CardRepository {

    Optional<Card> findByNumber(CardNumber cardNumber);

    /**
     * @throws CardModifiedConcurrentlyException gdy karta została w międzyczasie zmieniona przez inną transakcję
     */
    Card save(Card card);

}
