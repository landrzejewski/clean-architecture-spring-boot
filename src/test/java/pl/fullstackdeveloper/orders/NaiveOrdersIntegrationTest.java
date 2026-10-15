package pl.fullstackdeveloper.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.fullstackdeveloper.orders.application.GetOrderUseCase;
import pl.fullstackdeveloper.orders.application.PlaceOrderCommand;
import pl.fullstackdeveloper.orders.application.PlaceOrderUseCase;
import pl.fullstackdeveloper.payments.application.*;
import pl.fullstackdeveloper.payments.domain.Money;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

/*
 Test dokumentujący skutek obejścia use case'u płatności: ta sama płatność w EUR
 przechodzi przez AddTransactionUseCase, ale zamówienie w EUR zostaje odrzucone.
 */
@SpringBootTest
class NaiveOrdersIntegrationTest {

    @Autowired
    private AddCardUseCase addCard;
    @Autowired
    private AddTransactionUseCase addTransaction;
    @Autowired
    private PlaceOrderUseCase placeOrder;
    @Autowired
    private GetOrderUseCase getOrder;

    @Test
    void order_in_foreign_currency_is_rejected_although_payment_would_succeed() {
        var cardNumber = addCard.handle(new AddCardCommand(Currency.getInstance("PLN")));
        addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of("1000", "PLN"), INFLOW));
        addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of("10", "EUR"), PAYMENT));

        var orderId = placeOrder.handle(new PlaceOrderCommand("Book", cardNumber, Money.of("10", "EUR")));

        assertThat(getOrder.handle(orderId).status()).isEqualTo("REJECTED");
    }

}
