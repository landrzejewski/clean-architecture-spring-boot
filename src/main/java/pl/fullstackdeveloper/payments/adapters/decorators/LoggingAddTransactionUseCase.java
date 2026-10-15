package pl.fullstackdeveloper.payments.adapters.decorators;

import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionUseCase;
import pl.fullstackdeveloper.payments.domain.TransactionId;

import java.util.logging.Logger;

/*
 Klasyczny (statyczny) dekorator: implementuje ten sam interfejs i deleguje do oryginału.
 Zaleta: prosty i typowany. Wada: osobna klasa dla każdego interfejsu i każdego aspektu.
 Porównaj z Proxies + LoggingInterceptor.
 */
public final class LoggingAddTransactionUseCase implements AddTransactionUseCase {

    private static final Logger LOGGER = Logger.getLogger(LoggingAddTransactionUseCase.class.getName());

    private final AddTransactionUseCase delegate;

    public LoggingAddTransactionUseCase(final AddTransactionUseCase delegate) {
        this.delegate = delegate;
    }

    @Override
    public TransactionId handle(final AddTransactionCommand command) {
        LOGGER.info("Adding transaction: " + command);
        var transactionId = delegate.handle(command);
        LOGGER.info("Transaction added: " + transactionId);
        return transactionId;
    }

}
