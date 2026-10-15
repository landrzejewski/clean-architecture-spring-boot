package pl.fullstackdeveloper.payments;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.fullstackdeveloper.common.cqrs.Bus;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.domain.Money;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;

// Test "okablowania" strony zapisu: komendy przez szynę trafiają do use case'ów i bazy zapisu
@SpringBootTest
class PaymentsContextTest {

    @Autowired
    private Bus bus;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private org.springframework.transaction.support.TransactionTemplate transactionTemplate;

    @Test
    void commands_are_handled_by_use_cases() {
        var cardNumber = bus.send(new AddCardCommand(Currency.getInstance("EUR")));
        bus.send(new AddTransactionCommand(cardNumber, Money.of("10", "EUR"), INFLOW));

        var card = transactionTemplate.execute(status -> cardRepository.findByNumber(cardNumber).orElseThrow());
        assertThat(card.getBalance()).isEqualTo(Money.of("10", "EUR"));
    }

}
