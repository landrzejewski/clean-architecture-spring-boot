package pl.fullstackdeveloper.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.fullstackdeveloper.orders.application.GetOrderUseCase;
import pl.fullstackdeveloper.orders.application.PlaceOrderCommand;
import pl.fullstackdeveloper.orders.application.PlaceOrderUseCase;
import pl.fullstackdeveloper.orders.domain.CardReference;
import pl.fullstackdeveloper.orders.domain.Price;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddCardUseCase;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionUseCase;
import pl.fullstackdeveloper.payments.domain.Money;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;

@SpringBootTest
class OrdersIntegrationTest {

    @Autowired
    private AddCardUseCase addCard;
    @Autowired
    private AddTransactionUseCase addTransaction;
    @Autowired
    private PlaceOrderUseCase placeOrder;
    @Autowired
    private GetOrderUseCase getOrder;

    @Test
    void order_in_foreign_currency_is_paid_through_payments_api() {
        var card = cardWithBalance("1000");

        var orderId = placeOrder.handle(new PlaceOrderCommand("Book", card, Price.of("10", "EUR")));

        var order = getOrder.handle(orderId);
        assertThat(order.status()).isEqualTo("PAID");
        assertThat(order.paymentId()).isNotBlank();
    }

    @Test
    void order_is_rejected_when_payment_is_rejected() {
        var card = cardWithBalance("5");

        var orderId = placeOrder.handle(new PlaceOrderCommand("TV", card, Price.of("5000", "PLN")));

        var order = getOrder.handle(orderId);
        assertThat(order.status()).isEqualTo("REJECTED");
        assertThat(order.rejectionReason()).isEqualTo("Insufficient balance");
    }

    private CardReference cardWithBalance(final String amount) {
        var cardNumber = addCard.handle(new AddCardCommand(Currency.getInstance("PLN")));
        addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of(amount, "PLN"), INFLOW));
        return new CardReference(cardNumber.value());
    }

}
