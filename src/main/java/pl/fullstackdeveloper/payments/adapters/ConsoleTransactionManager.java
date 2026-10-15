package pl.fullstackdeveloper.payments.adapters;

import pl.fullstackdeveloper.common.proxy.TransactionManager;

import java.util.logging.Logger;

// Symulacja - prawdziwe transakcje dostarczy Spring w 05/07
public final class ConsoleTransactionManager implements TransactionManager {

    private static final Logger LOGGER = Logger.getLogger(ConsoleTransactionManager.class.getName());

    @Override
    public void begin() {
        LOGGER.info("Transaction started");
    }

    @Override
    public void commit() {
        LOGGER.info("Transaction committed");
    }

    @Override
    public void rollback() {
        LOGGER.info("Transaction rolled back");
    }

}
