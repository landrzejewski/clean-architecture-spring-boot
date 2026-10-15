package pl.fullstackdeveloper.payments.queries;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.payments.application.CardAdded;
import pl.fullstackdeveloper.payments.application.PaymentsEventHistory;
import pl.fullstackdeveloper.payments.application.TransactionAdded;

import java.util.concurrent.atomic.AtomicLong;

/*
 Replay: model odczytu budujemy od zera, przepuszczając CAŁĄ historię zdarzeń przez tę samą projekcję,
 która działa na bieżąco. W Event Sourcingu nowy widok (nawet wymyślony po latach) dostaje pełną historię.
 */
@Component
public class CardProjectionRebuilder {

    private final PaymentsEventHistory history;
    private final CardSummaryProjector projector;
    private final MongoTemplate mongoTemplate;

    CardProjectionRebuilder(final PaymentsEventHistory history, final CardSummaryProjector projector, final MongoTemplate mongoTemplate) {
        this.history = history;
        this.projector = projector;
        this.mongoTemplate = mongoTemplate;
    }

    public long rebuild() {
        mongoTemplate.dropCollection(CardSummaryDocument.class);
        var replayed = new AtomicLong();
        history.replay(event -> {
            switch (event) {
                case CardAdded cardAdded -> projector.on(cardAdded);
                case TransactionAdded transactionAdded -> projector.on(transactionAdded);
            }
            replayed.incrementAndGet();
        });
        return replayed.get();
    }

}
