package pl.fullstackdeveloper.payments;

import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.ResultPage;
import pl.fullstackdeveloper.common.proxy.AtomicInterceptor;
import pl.fullstackdeveloper.common.proxy.LoggingInterceptor;
import pl.fullstackdeveloper.common.proxy.Proxies;
import pl.fullstackdeveloper.common.proxy.TimerInterceptor;
import pl.fullstackdeveloper.payments.adapters.decorators.LoggingAddTransactionUseCase;
import pl.fullstackdeveloper.payments.application.*;
import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.TransactionId;

/*
 Facade - jeden, prosty punkt wejścia do modułu płatności. Ukrywa składanie use case'ów,
 portów i proxy (composition root modułu). Klient nie wie, ile obiektów jest pod spodem.
 */
public final class PaymentsFacade {

    private final AddCardUseCase addCard;
    private final AddTransactionUseCase addTransaction;
    private final GetCardUseCase getCard;
    private final GetCardsUseCase getCards;

    public PaymentsFacade(final PaymentsFactory factory) {
        var cardRepository = factory.cardRepository();
        var atomic = new AtomicInterceptor(factory.transactionManager());
        var timer = new TimerInterceptor();
        var logging = new LoggingInterceptor();

        AddCardUseCase addCardService = new AddCardService(factory.cardNumberGenerator(), factory.dateTimeProvider(), cardRepository);
        AddTransactionUseCase addTransactionService = new AddTransactionService(factory.dateTimeProvider(), factory.transactionEventPublisher(), cardRepository);

        // kolejność interceptorów ma znaczenie: logging -> timer -> atomic -> target
        addCard = Proxies.wrap(AddCardUseCase.class, addCardService, logging, atomic);
        // statyczny dekorator i dynamiczny proxy można łączyć
        addTransaction = new LoggingAddTransactionUseCase(
                Proxies.wrap(AddTransactionUseCase.class, addTransactionService, timer, atomic));
        getCard = new GetCardService(cardRepository);
        getCards = new GetCardsService(cardRepository);
    }

    public CardNumber addCard(final AddCardCommand command) {
        return addCard.handle(command);
    }

    public TransactionId addTransaction(final AddTransactionCommand command) {
        return addTransaction.handle(command);
    }

    public CardView getCard(final CardNumber cardNumber) {
        return getCard.handle(cardNumber);
    }

    public ResultPage<CardView> getCards(final PageSpec pageSpec) {
        return getCards.handle(pageSpec);
    }

}
