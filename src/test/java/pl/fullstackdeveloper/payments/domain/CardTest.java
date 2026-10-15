package pl.fullstackdeveloper.payments.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.fullstackdeveloper.payments.domain.CardTestFixtures.*;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;

/*
 Czyste testy jednostkowe: bez Springa, bazy i zegara systemowego - wykonują się w milisekundach.
 */
class CardTest {

    @Test
    void new_card_has_zero_balance() {
        var card = plnCard();

        assertThat(card.getBalance()).isEqualTo(Money.zero(PLN));
    }

    @Test
    void inflow_increases_balance() {
        var card = plnCard();

        card.registerTransaction(inflow("100"));

        assertThat(card.getBalance()).isEqualTo(Money.of("100", "PLN"));
    }

    @Test
    void payment_decreases_balance() {
        var card = plnCard();
        card.registerTransaction(inflow("100"));

        card.registerTransaction(payment("40"));

        assertThat(card.getBalance()).isEqualTo(Money.of("60", "PLN"));
    }

    @Test
    void payment_is_rejected_when_balance_is_insufficient() {
        var card = plnCard();

        assertThatThrownBy(() -> card.registerTransaction(payment("1")))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(card.getTransactionsCount()).isZero();
    }

    @Test
    void transaction_in_other_currency_is_rejected() {
        var card = plnCard();

        assertThatThrownBy(() -> card.registerTransaction(transaction("10", EUR, INFLOW, BEFORE_EXPIRATION)))
                .isInstanceOf(MismatchedCurrencyException.class);
    }

    @Test
    void transaction_after_expiration_is_rejected() {
        var card = plnCard();

        assertThatThrownBy(() -> card.registerTransaction(transaction("10", PLN, INFLOW, AFTER_EXPIRATION)))
                .isInstanceOf(InvalidTransactionTimestampException.class);
    }

    @Test
    void registered_transaction_produces_domain_event() {
        var card = plnCard();
        card.pullEvents();
        var transaction = inflow("100");

        card.registerTransaction(transaction);

        assertThat(card.pullEvents()).containsExactly(new TransactionRegistered(CARD_NUMBER, transaction));
        assertThat(card.pullEvents()).isEmpty();
    }

}
