package pl.fullstackdeveloper.payments.queries;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.cqrs.Bus;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.domain.Money;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

// Komendy zmieniają stan przez agregat, zapytania czytają go bezpośrednio z bazy - obie strony przez jedną szynę
@SpringBootTest
class CardQueriesTest {

    @Autowired
    private Bus bus;

    @Test
    void summary_reflects_commands() {
        var cardNumber = bus.send(new AddCardCommand(Currency.getInstance("PLN")));
        bus.send(new AddTransactionCommand(cardNumber, Money.of("100", "PLN"), INFLOW));
        bus.send(new AddTransactionCommand(cardNumber, Money.of("30", "PLN"), PAYMENT));

        var summary = bus.ask(new GetCardSummaryQuery(cardNumber.value())).orElseThrow();

        assertThat(summary.balance()).isEqualByComparingTo("70");
        assertThat(summary.transactionsCount()).isEqualTo(2);
    }

    @Test
    void transactions_are_listed_newest_first() {
        var cardNumber = bus.send(new AddCardCommand(Currency.getInstance("PLN")));
        bus.send(new AddTransactionCommand(cardNumber, Money.of("100", "PLN"), INFLOW));
        bus.send(new AddTransactionCommand(cardNumber, Money.of("30", "PLN"), PAYMENT));

        var transactions = bus.ask(new GetCardTransactionsQuery(cardNumber.value()));

        assertThat(transactions).extracting(TransactionSummary::type).containsExactly("PAYMENT", "INFLOW");
    }

    @Test
    void missing_card_gives_empty_result() {
        assertThat(bus.ask(new GetCardSummaryQuery("0000000000000000"))).isEmpty();
    }

    @Test
    void cards_are_paged() {
        bus.send(new AddCardCommand(Currency.getInstance("PLN")));

        var page = bus.ask(new GetCardsQuery(new PageSpec(0, 1)));

        assertThat(page.content()).hasSize(1);
    }

}
