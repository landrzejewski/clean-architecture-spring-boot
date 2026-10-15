package pl.fullstackdeveloper;

import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.payments.DevPaymentsFactory;
import pl.fullstackdeveloper.payments.PaymentsFacade;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.domain.InsufficientBalanceException;
import pl.fullstackdeveloper.payments.domain.Money;

import java.util.Currency;

import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

public class Application {

    public static void main(String[] args) {
        var payments = new PaymentsFacade(new DevPaymentsFactory());

        var cardNumber = payments.addCard(new AddCardCommand(Currency.getInstance("PLN")));
        payments.addTransaction(new AddTransactionCommand(cardNumber, Money.of("1000", "PLN"), INFLOW));
        try {
            payments.addTransaction(new AddTransactionCommand(cardNumber, Money.of("5000", "PLN"), PAYMENT));
        } catch (InsufficientBalanceException exception) {
            System.out.println("Rejected: " + exception.getMessage());
        }
        System.out.println(payments.getCards(new PageSpec(0, 10)));
    }

}
