package pl.fullstackdeveloper.payments.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.fullstackdeveloper.payments.domain.CardTestFixtures.*;

/*
 Testy w stylu Event Sourcing: given (historia zdarzeń) / when (komenda) / then (nowe zdarzenia).
 Stan agregatu jest szczegółem - kontraktem są zdarzenia.
 */
class CardEventSourcingTest {

    private static final CardId CARD_ID = new CardId();
    private static final CardIssued ISSUED = new CardIssued(CARD_ID, CARD_NUMBER, EXPIRATION, PLN);

    @Test
    void new_card_emits_card_issued() {
        var card = new Card(CARD_ID, CARD_NUMBER, EXPIRATION, PLN);

        assertThat(card.pullEvents()).containsExactly(ISSUED);
        assertThat(card.getVersion()).isEqualTo(1);
    }

    @Test
    void state_is_rebuilt_from_history() {
        var card = Card.fromHistory(List.of(ISSUED, registered(inflow("100")), registered(payment("30"))));

        assertThat(card.getBalance()).isEqualTo(Money.of("70", "PLN"));
        assertThat(card.getVersion()).isEqualTo(3);
        assertThat(card.getPendingEvents()).isEmpty();
    }

    @Test
    void given_history_when_payment_then_transaction_registered() {
        var card = Card.fromHistory(List.of(ISSUED, registered(inflow("100"))));
        var payment = payment("40");

        card.registerTransaction(payment);

        assertThat(card.pullEvents()).containsExactly(registered(payment));
    }

    @Test
    void given_history_when_payment_exceeds_balance_then_no_events() {
        var card = Card.fromHistory(List.of(ISSUED, registered(inflow("10"))));

        assertThatThrownBy(() -> card.registerTransaction(payment("40"))).isInstanceOf(InsufficientBalanceException.class);
        assertThat(card.getPendingEvents()).isEmpty();
    }

    @Test
    void snapshot_plus_newer_events_equals_full_history() {
        var fullHistory = List.of(ISSUED, registered(inflow("100")), registered(payment("30")), registered(inflow("5")));
        var snapshot = Card.fromHistory(fullHistory.subList(0, 2)).snapshot();

        var fromSnapshot = Card.fromSnapshot(snapshot, fullHistory.subList(2, 4));

        assertThat(fromSnapshot.snapshot()).isEqualTo(Card.fromHistory(fullHistory).snapshot());
    }

    private static TransactionRegistered registered(final Transaction transaction) {
        return new TransactionRegistered(CARD_NUMBER, transaction);
    }

}
