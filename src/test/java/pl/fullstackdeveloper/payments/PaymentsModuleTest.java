package pl.fullstackdeveloper.payments;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import pl.fullstackdeveloper.payments.api.PaymentProcessed;
import pl.fullstackdeveloper.payments.api.PaymentRequested;
import pl.fullstackdeveloper.payments.api.PaymentResult;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddCardUseCase;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionUseCase;
import pl.fullstackdeveloper.payments.domain.Money;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;

@ApplicationModuleTest(extraIncludes = "common")
@org.springframework.context.annotation.Import(PaymentsModuleTest.CardBalances.class)
class PaymentsModuleTest {

    @Autowired
    private AddCardUseCase addCard;
    @Autowired
    private AddTransactionUseCase addTransaction;
    @Autowired
    private CardBalances cardRepository;

    // pomocniczy odczyt salda w transakcji
    @org.springframework.boot.test.context.TestComponent
    static class CardBalances {

        private final pl.fullstackdeveloper.payments.application.CardRepository cards;
        private final org.springframework.transaction.support.TransactionTemplate transactionTemplate;

        CardBalances(final pl.fullstackdeveloper.payments.application.CardRepository cards,
                     final org.springframework.transaction.support.TransactionTemplate transactionTemplate) {
            this.cards = cards;
            this.transactionTemplate = transactionTemplate;
        }

        Money findBalance(final pl.fullstackdeveloper.payments.domain.CardNumber cardNumber) {
            return transactionTemplate.execute(status -> cards.findByNumber(cardNumber).orElseThrow().getBalance());
        }

    }

    @Test
    void payment_request_is_processed_and_answered_with_event(final Scenario scenario) {
        var cardNumber = addCard.handle(new AddCardCommand(Currency.getInstance("PLN")));
        addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of("100", "PLN"), INFLOW));

        scenario.publish(new PaymentRequested("order-1", cardNumber.value(), new BigDecimal("30"), "PLN"))
                .andWaitForEventOfType(PaymentProcessed.class)
                .matching(event -> event.reference().equals("order-1"))
                .toArriveAndVerify(event -> assertThat(event.result()).isInstanceOf(PaymentResult.Completed.class));
    }

    @Test
    void rejected_payment_is_answered_with_rejection(final Scenario scenario) {
        var cardNumber = addCard.handle(new AddCardCommand(Currency.getInstance("PLN")));

        scenario.publish(new PaymentRequested("order-2", cardNumber.value(), new BigDecimal("30"), "PLN"))
                .andWaitForEventOfType(PaymentProcessed.class)
                .matching(event -> event.reference().equals("order-2"))
                .toArriveAndVerify(event -> assertThat(event.result()).isEqualTo(new PaymentResult.Rejected("Insufficient balance")));
    }


    @Test
    void duplicated_payment_request_charges_card_only_once(final Scenario scenario) {
        var cardNumber = addCard.handle(new AddCardCommand(Currency.getInstance("PLN")));
        addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of("100", "PLN"), INFLOW));
        var request = new PaymentRequested("order-3", cardNumber.value(), new BigDecimal("30"), "PLN");

        scenario.publish(request)
                .andWaitForEventOfType(PaymentProcessed.class)
                .matching(event -> event.reference().equals("order-3"))
                .toArrive();
        scenario.publish(request)
                .andWaitForEventOfType(PaymentProcessed.class)
                .matching(event -> event.reference().equals("order-3"))
                .toArriveAndVerify(event -> assertThat(event.result()).isInstanceOf(PaymentResult.Completed.class));

        assertThat(cardRepository.findBalance(cardNumber)).isEqualTo(Money.of("70", "PLN"));
    }

}
