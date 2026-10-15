package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.Money;

import java.time.LocalDate;

// Model wyjściowy use case'u - agregat nie wycieka poza warstwę aplikacji
public record CardView(String number, LocalDate expiration, Money balance, int transactionsCount) {

    static CardView from(final Card card) {
        return new CardView(card.getNumber().value(), card.getExpiration(), card.getBalance(), card.getTransactions().size());
    }

}
