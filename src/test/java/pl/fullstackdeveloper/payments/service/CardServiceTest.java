package pl.fullstackdeveloper.payments.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 Żeby sprawdzić prostą regułę biznesową (brak środków), musimy podnieść cały kontekst Springa i bazę danych.
 Nie da się też kontrolować czasu (ZonedDateTime.now()) ani numeru karty (Random).
 */
@SpringBootTest
class CardServiceTest {

    @Autowired
    private CardService cardService;

    @Test
    void should_reject_payment_when_balance_is_insufficient() {
        var card = cardService.addCard("PLN");

        assertThatThrownBy(() -> cardService.addTransaction(card.getNumber(), new BigDecimal("100"), "PLN", "OUT"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Insufficient balance");
    }

    @Test
    void should_increase_balance_after_inflow() {
        var card = cardService.addCard("PLN");

        var updatedCard = cardService.addTransaction(card.getNumber(), new BigDecimal("100"), "PLN", "IN");

        assertThat(updatedCard.getBalance()).isEqualByComparingTo("100");
    }

}
