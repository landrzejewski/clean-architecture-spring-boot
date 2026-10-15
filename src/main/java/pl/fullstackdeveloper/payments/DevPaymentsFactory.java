package pl.fullstackdeveloper.payments;

import pl.fullstackdeveloper.common.proxy.TransactionManager;
import pl.fullstackdeveloper.payments.adapters.*;
import pl.fullstackdeveloper.payments.application.CardNumberGenerator;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.application.DateTimeProvider;
import pl.fullstackdeveloper.payments.application.TransactionEventPublisher;

public final class DevPaymentsFactory implements PaymentsFactory {

    private static final int CARD_NUMBER_LENGTH = 16;

    private final CardRepository cardRepository = new HashMapCardRepository();

    @Override
    public CardRepository cardRepository() {
        return cardRepository;
    }

    @Override
    public DateTimeProvider dateTimeProvider() {
        return new SystemDateTimeProvider();
    }

    @Override
    public CardNumberGenerator cardNumberGenerator() {
        return new RandomCardNumberGenerator(CARD_NUMBER_LENGTH);
    }

    @Override
    public TransactionEventPublisher transactionEventPublisher() {
        return new ConsoleTransactionEventPublisher();
    }

    @Override
    public TransactionManager transactionManager() {
        return new ConsoleTransactionManager();
    }

}
