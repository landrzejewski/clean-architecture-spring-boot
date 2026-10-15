package pl.fullstackdeveloper.payments.queries;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.cqrs.Bus;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.domain.Money;

import java.time.Duration;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

/*
 Model odczytu jest budowany asynchronicznie ze zdarzeń - test musi poczekać, aż projekcja "dogoni" zapis.
 */
@SpringBootTest
@Import(MongoReadStore.class)
@EnabledIf("pl.fullstackdeveloper.payments.queries.Docker#isAvailable")
class CardQueriesTest {

    @Autowired
    private Bus bus;
    @Autowired
    private CardProjectionRebuilder rebuilder;

    @Test
    void summary_eventually_reflects_commands() {
        var cardNumber = bus.send(new AddCardCommand(Currency.getInstance("PLN")));
        bus.send(new AddTransactionCommand(cardNumber, Money.of("100", "PLN"), INFLOW));
        bus.send(new AddTransactionCommand(cardNumber, Money.of("30", "PLN"), PAYMENT));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            var summary = bus.ask(new GetCardSummaryQuery(cardNumber.value())).orElseThrow();
            assertThat(summary.balance()).isEqualByComparingTo("70");
            assertThat(summary.transactionsCount()).isEqualTo(2);
            assertThat(summary.expiration()).isNotNull();
        });
        assertThat(bus.ask(new GetCardTransactionsQuery(cardNumber.value())))
                .extracting(TransactionSummary::type).containsExactly("PAYMENT", "INFLOW");
    }

    @Test
    void read_model_can_be_rebuilt_from_write_side() {
        var cardNumber = bus.send(new AddCardCommand(Currency.getInstance("PLN")));
        bus.send(new AddTransactionCommand(cardNumber, Money.of("50", "PLN"), INFLOW));
        await().atMost(Duration.ofSeconds(10)).until(() -> bus.ask(new GetCardSummaryQuery(cardNumber.value())).isPresent());

        rebuilder.rebuild();

        var summary = bus.ask(new GetCardSummaryQuery(cardNumber.value())).orElseThrow();
        assertThat(summary.balance()).isEqualByComparingTo("50");
        assertThat(bus.ask(new GetCardsQuery(new PageSpec(0, 100))).content()).isNotEmpty();
    }

}
