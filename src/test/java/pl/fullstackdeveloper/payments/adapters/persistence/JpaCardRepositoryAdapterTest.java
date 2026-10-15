package pl.fullstackdeveloper.payments.adapters.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.payments.domain.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

/*
 Test adaptera persystencji na prawdziwym JPA (H2): tylko warstwa danych + nasz adapter i mapper.
 Sprawdzamy, że agregat po zapisie i odczycie jest "taki sam" - kontrakt portu CardRepository.
 */
@DataJpaTest
@Import({JpaCardRepositoryAdapter.class, JpaCardMapper.class})
class JpaCardRepositoryAdapterTest {

    private static final CardNumber NUMBER = new CardNumber("1111222233334444");

    @Autowired
    private JpaCardRepositoryAdapter adapter;

    @Test
    void card_with_transactions_survives_round_trip() {
        var card = new Card(new CardId(), NUMBER, LocalDate.now().plusYears(1), Currency.getInstance("PLN"));
        card.registerTransaction(new Transaction(new TransactionId(), ZonedDateTime.now(), Money.of("100", "PLN"), TransactionType.INFLOW));
        card.registerTransaction(new Transaction(new TransactionId(), ZonedDateTime.now(), Money.of("30", "PLN"), TransactionType.PAYMENT));

        adapter.save(card);
        var loaded = adapter.findByNumber(NUMBER).orElseThrow();

        assertThat(loaded.getId()).isEqualTo(card.getId());
        assertThat(loaded.getBalance()).isEqualTo(Money.of("70", "PLN"));
        assertThat(loaded.getTransactions()).extracting(Transaction::id)
                .containsExactlyElementsOf(card.getTransactions().stream().map(Transaction::id).toList());
    }

    @Test
    void cards_are_paged() {
        for (int i = 0; i < 3; i++) {
            adapter.save(new Card(new CardId(), new CardNumber("100000000000000" + i), LocalDate.now().plusYears(1), Currency.getInstance("PLN")));
        }

        var page = adapter.findAll(new PageSpec(1, 2));

        assertThat(page.content()).hasSize(1);
        assertThat(page.totalPages()).isEqualTo(2);
    }

}
