package pl.fullstackdeveloper.payments;

import pl.fullstackdeveloper.common.proxy.TransactionManager;
import pl.fullstackdeveloper.payments.application.CardNumberGenerator;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.application.DateTimeProvider;
import pl.fullstackdeveloper.payments.application.TransactionEventPublisher;

// Abstract Factory - rodzina spójnych implementacji portów wyjściowych dla danego środowiska
public interface PaymentsFactory {

    CardRepository cardRepository();

    DateTimeProvider dateTimeProvider();

    CardNumberGenerator cardNumberGenerator();

    TransactionEventPublisher transactionEventPublisher();

    TransactionManager transactionManager();

}
