package pl.fullstackdeveloper.payments;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import pl.fullstackdeveloper.events.OrderPlaced;
import pl.fullstackdeveloper.events.PaymentCompleted;
import pl.fullstackdeveloper.events.PaymentFailed;
import pl.fullstackdeveloper.events.PaymentRefunded;
import pl.fullstackdeveloper.events.ShipmentFailed;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddCardUseCase;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionUseCase;
import pl.fullstackdeveloper.payments.domain.Money;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;

@ApplicationModuleTest(extraIncludes = {"common", "events"})
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
    void order_placed_is_answered_with_payment_completed(final Scenario scenario) {
        var cardNumber = cardWithBalance("100");

        scenario.publish(new OrderPlaced("order-1", cardNumber.value(), new BigDecimal("30"), "PLN", "Warszawa"))
                .andWaitForEventOfType(PaymentCompleted.class)
                .matching(event -> event.orderId().equals("order-1"))
                .toArrive();

        assertThat(cardRepository.findBalance(cardNumber)).isEqualTo(Money.of("70", "PLN"));
    }

    @Test
    void order_without_funds_is_answered_with_payment_failed(final Scenario scenario) {
        var cardNumber = cardWithBalance("10");

        scenario.publish(new OrderPlaced("order-2", cardNumber.value(), new BigDecimal("30"), "PLN", "Warszawa"))
                .andWaitForEventOfType(PaymentFailed.class)
                .matching(event -> event.orderId().equals("order-2"))
                .toArriveAndVerify(event -> assertThat(event.reason()).isEqualTo("Insufficient balance"));
    }

    @Test
    void failed_shipment_is_compensated_with_refund(final Scenario scenario) {
        var cardNumber = cardWithBalance("100");
        scenario.publish(new OrderPlaced("order-3", cardNumber.value(), new BigDecimal("30"), "PLN", "Antarctica"))
                .andWaitForEventOfType(PaymentCompleted.class)
                .matching(event -> event.orderId().equals("order-3"))
                .toArrive();

        scenario.publish(new ShipmentFailed("order-3", "Address is outside of the delivery area"))
                .andWaitForEventOfType(PaymentRefunded.class)
                .matching(event -> event.orderId().equals("order-3"))
                .toArrive();

        assertThat(cardRepository.findBalance(cardNumber)).isEqualTo(Money.of("100", "PLN"));
    }

    @Test
    void duplicated_order_placed_charges_card_only_once(final Scenario scenario) {
        var cardNumber = cardWithBalance("100");
        var event = new OrderPlaced("order-4", cardNumber.value(), new BigDecimal("30"), "PLN", "Warszawa");

        scenario.publish(event).andWaitForEventOfType(PaymentCompleted.class).matching(e -> e.orderId().equals("order-4")).toArrive();
        scenario.publish(event).andWaitForEventOfType(PaymentCompleted.class).matching(e -> e.orderId().equals("order-4")).toArrive();

        assertThat(cardRepository.findBalance(cardNumber)).isEqualTo(Money.of("70", "PLN"));
    }

    private pl.fullstackdeveloper.payments.domain.CardNumber cardWithBalance(final String amount) {
        var cardNumber = addCard.handle(new AddCardCommand(Currency.getInstance("PLN")));
        addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of(amount, "PLN"), INFLOW));
        return cardNumber;
    }

}
