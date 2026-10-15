package pl.fullstackdeveloper.payments;

import pl.fullstackdeveloper.common.proxy.TransactionManager;
import pl.fullstackdeveloper.payments.adapters.ConsoleTransactionEventPublisher;
import pl.fullstackdeveloper.payments.adapters.HashMapCardRepository;
import pl.fullstackdeveloper.payments.application.CardNumberGenerator;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.application.DateTimeProvider;
import pl.fullstackdeveloper.payments.application.TransactionEventPublisher;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

// Druga "rodzina" implementacji: przewidywalne numery kart, stały zegar, rejestrowanie transakcji
final class TestPaymentsFactory implements PaymentsFactory {

    static final ZonedDateTime NOW = ZonedDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/Warsaw"));

    final List<String> transactionLog = new ArrayList<>();
    private final CardRepository cardRepository = new HashMapCardRepository();
    private final AtomicLong sequence = new AtomicLong(1_000_000_000_000_000L);

    @Override
    public CardRepository cardRepository() {
        return cardRepository;
    }

    @Override
    public DateTimeProvider dateTimeProvider() {
        return () -> NOW;
    }

    @Override
    public CardNumberGenerator cardNumberGenerator() {
        return () -> new CardNumber(Long.toString(sequence.incrementAndGet()));
    }

    @Override
    public TransactionEventPublisher transactionEventPublisher() {
        return new ConsoleTransactionEventPublisher();
    }

    @Override
    public TransactionManager transactionManager() {
        return new TransactionManager() {

            @Override
            public void begin() {
                transactionLog.add("begin");
            }

            @Override
            public void commit() {
                transactionLog.add("commit");
            }

            @Override
            public void rollback() {
                transactionLog.add("rollback");
            }

        };
    }

}
