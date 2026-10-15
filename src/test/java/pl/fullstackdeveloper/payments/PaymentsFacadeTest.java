package pl.fullstackdeveloper.payments;

import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.domain.InsufficientBalanceException;
import pl.fullstackdeveloper.payments.domain.Money;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

class PaymentsFacadeTest {

    private final TestPaymentsFactory factory = new TestPaymentsFactory();
    private final PaymentsFacade payments = new PaymentsFacade(factory);

    @Test
    void card_numbers_come_from_test_factory() {
        var cardNumber = payments.addCard(new AddCardCommand(Currency.getInstance("PLN")));

        assertThat(cardNumber.value()).isEqualTo("1000000000000001");
        assertThat(payments.getCard(cardNumber).expiration()).isEqualTo(TestPaymentsFactory.NOW.toLocalDate().plusYears(1));
    }

    @Test
    void use_cases_run_inside_transactions() {
        var cardNumber = payments.addCard(new AddCardCommand(Currency.getInstance("PLN")));
        payments.addTransaction(new AddTransactionCommand(cardNumber, Money.of("100", "PLN"), INFLOW));

        assertThatThrownBy(() -> payments.addTransaction(new AddTransactionCommand(cardNumber, Money.of("500", "PLN"), PAYMENT)))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(factory.transactionLog).containsExactly("begin", "commit", "begin", "commit", "begin", "rollback");
    }

}
