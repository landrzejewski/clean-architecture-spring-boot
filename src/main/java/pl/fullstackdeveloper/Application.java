package pl.fullstackdeveloper;

import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.payments.adapters.ConsoleTransactionEventPublisher;
import pl.fullstackdeveloper.payments.adapters.HashMapCardRepository;
import pl.fullstackdeveloper.payments.adapters.RandomCardNumberGenerator;
import pl.fullstackdeveloper.payments.adapters.SystemDateTimeProvider;
import pl.fullstackdeveloper.payments.application.*;
import pl.fullstackdeveloper.payments.domain.Money;

import java.util.Currency;

import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

/*
 Composition root - jedyne miejsce, które zna wszystkie implementacje i składa z nich aplikację.
 Ręczne wstrzykiwanie zależności przez konstruktory, bez frameworka.
 */
public class Application {

    private static final int CARD_NUMBER_LENGTH = 16;

    public static void main(String[] args) {
        // adaptery (infrastruktura)
        var cardRepository = new HashMapCardRepository();
        var dateTimeProvider = new SystemDateTimeProvider();
        var cardNumberGenerator = new RandomCardNumberGenerator(CARD_NUMBER_LENGTH);
        var eventPublisher = new ConsoleTransactionEventPublisher();

        // use case'y (aplikacja)
        AddCardUseCase addCard = new AddCardService(cardNumberGenerator, dateTimeProvider, cardRepository);
        AddTransactionUseCase addTransaction = new AddTransactionService(dateTimeProvider, eventPublisher, cardRepository);
        GetCardsUseCase getCards = new GetCardsService(cardRepository);

        // "adapter wejściowy" - tu po prostu main(), w kolejnych krokach REST
        var cardNumber = addCard.handle(new AddCardCommand(Currency.getInstance("PLN")));
        addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of("1000", "PLN"), INFLOW));
        addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of("200", "PLN"), PAYMENT));
        System.out.println(getCards.handle(new PageSpec(0, 10)));
    }

}
