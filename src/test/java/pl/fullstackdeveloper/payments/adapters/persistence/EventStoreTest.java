package pl.fullstackdeveloper.payments.adapters.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.domain.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "payments.snapshot-frequency=10")
class EventStoreTest {

    private static final CardNumber NUMBER = new CardNumber("4444333322221111");

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void every_change_is_appended_and_snapshots_are_taken_every_n_events() {
        transactionTemplate.executeWithoutResult(status ->
                cardRepository.save(new Card(new CardId(), NUMBER, LocalDate.now().plusYears(1), Currency.getInstance("PLN"))));
        for (int i = 0; i < 24; i++) {
            transactionTemplate.executeWithoutResult(status -> {
                var card = cardRepository.findByNumber(NUMBER).orElseThrow();
                card.registerTransaction(new Transaction(new TransactionId(), ZonedDateTime.now(), Money.of("1", "PLN"), TransactionType.INFLOW));
                cardRepository.save(card);
            });
        }

        // 1 x CardIssued + 24 x TransactionRegistered = 25 zdarzeń, ostatnia migawka po 20. zdarzeniu
        assertThat(count("select count(*) from card_events where stream_id = :id")).isEqualTo(25);
        assertThat(count("select version from card_snapshots where stream_id = :id")).isEqualTo(20);
        var card = transactionTemplate.execute(status -> cardRepository.findByNumber(NUMBER).orElseThrow());
        assertThat(card.getBalance()).isEqualTo(Money.of("24", "PLN"));
        assertThat(card.getVersion()).isEqualTo(25);
    }

    private long count(final String sql) {
        return jdbcClient.sql(sql).param("id", NUMBER.value()).query(Long.class).single();
    }

}
