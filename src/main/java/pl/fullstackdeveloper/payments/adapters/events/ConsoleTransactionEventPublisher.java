package pl.fullstackdeveloper.payments.adapters.events;

import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.TransactionAdded;
import pl.fullstackdeveloper.payments.application.TransactionEventPublisher;

import java.util.logging.Logger;

@Adapter
final class ConsoleTransactionEventPublisher implements TransactionEventPublisher {

    private static final Logger LOGGER = Logger.getLogger(ConsoleTransactionEventPublisher.class.getName());

    @Override
    public void publish(final TransactionAdded event) {
        LOGGER.info("New %s transaction on card %s: %s %s".formatted(event.type(), event.cardNumber(), event.amount(), event.currencyCode()));
    }

}
