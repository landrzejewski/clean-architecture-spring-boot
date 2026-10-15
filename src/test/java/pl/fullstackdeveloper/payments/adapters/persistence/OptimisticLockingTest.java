package pl.fullstackdeveloper.payments.adapters.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import pl.fullstackdeveloper.payments.application.CardModifiedConcurrentlyException;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.domain.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
 Dwie "równoległe" transakcje modyfikują tę samą kartę. Druga, pracująca na nieaktualnej wersji,
 dostaje wyjątek zdefiniowany przez port (nie wyjątek Hibernate) - use case może go obsłużyć (@Retry).
 */
@SpringBootTest
class OptimisticLockingTest {

    private static final CardNumber NUMBER = new CardNumber("5555666677778888");

    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void stale_card_cannot_be_saved() {
        var transaction = new TransactionTemplate(transactionManager);
        var newTransaction = new TransactionTemplate(transactionManager);
        newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transaction.executeWithoutResult(status -> cardRepository.save(
                new Card(new CardId(), NUMBER, LocalDate.now().plusYears(1), Currency.getInstance("PLN"))));

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            var staleCard = cardRepository.findByNumber(NUMBER).orElseThrow();
            // w międzyczasie ktoś inny dodaje transakcję i zatwierdza zmiany
            newTransaction.executeWithoutResult(inner -> addInflow(cardRepository.findByNumber(NUMBER).orElseThrow()));
            addInflow(staleCard);
        })).isInstanceOf(CardModifiedConcurrentlyException.class);

        var balance = transaction.execute(status -> cardRepository.findByNumber(NUMBER).orElseThrow().getBalance());
        assertThat(balance).isEqualTo(Money.of("10", "PLN"));
    }

    private void addInflow(final Card card) {
        card.registerTransaction(new Transaction(new TransactionId(), ZonedDateTime.now(), Money.of("10", "PLN"), TransactionType.INFLOW));
        cardRepository.save(card);
    }

}
