package pl.fullstackdeveloper.payments.adapters.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.domain.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/*
 Test kontraktu portu CardRepository - te same scenariusze uruchamiamy dla każdej implementacji
 (JPA i JDBC). Gwarantuje to, że adaptery są naprawdę wymienne.
 */
abstract class CardRepositoryContractTest {

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void saved_card_can_be_found_by_number() {
        var card = newCard();
        card.registerTransaction(transaction("100", TransactionType.INFLOW));

        inTransaction(() -> cardRepository.save(card));
        var loaded = inTransaction(() -> cardRepository.findByNumber(card.getNumber()).orElseThrow());

        assertThat(loaded.getId()).isEqualTo(card.getId());
        assertThat(loaded.getBalance()).isEqualTo(Money.of("100", "PLN"));
    }

    @Test
    void new_transactions_are_appended_to_existing_card() {
        var card = newCard();
        inTransaction(() -> cardRepository.save(card));

        inTransaction(() -> {
            var loaded = cardRepository.findByNumber(card.getNumber()).orElseThrow();
            loaded.registerTransaction(transaction("50", TransactionType.INFLOW));
            loaded.registerTransaction(transaction("20", TransactionType.PAYMENT));
            return cardRepository.save(loaded);
        });

        var reloaded = inTransaction(() -> cardRepository.findByNumber(card.getNumber()).orElseThrow());
        assertThat(reloaded.getTransactions()).hasSize(2);
        assertThat(reloaded.getBalance()).isEqualTo(Money.of("30", "PLN"));
    }

    @Test
    void missing_card_is_not_found() {
        var number = randomNumber();

        assertThat(inTransaction(() -> cardRepository.findByNumber(number))).isEmpty();
    }

    private Card newCard() {
        return new Card(new CardId(), randomNumber(), LocalDate.now().plusYears(1), Currency.getInstance("PLN"));
    }

    private CardNumber randomNumber() {
        return new CardNumber(Long.toString(ThreadLocalRandom.current().nextLong(1_000_000_000_000_000L, 9_999_999_999_999_999L)));
    }

    private Transaction transaction(final String amount, final TransactionType type) {
        return new Transaction(new TransactionId(), ZonedDateTime.now(), Money.of(amount, "PLN"), type);
    }

    private <T> T inTransaction(final java.util.function.Supplier<T> action) {
        return new TransactionTemplate(transactionManager).execute(status -> action.get());
    }

}
