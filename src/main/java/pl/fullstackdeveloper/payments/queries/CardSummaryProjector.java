package pl.fullstackdeveloper.payments.queries;

import org.bson.types.Decimal128;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.payments.application.CardAdded;
import pl.fullstackdeveloper.payments.application.TransactionAdded;

/*
 Projekcja: zdarzenia ze strony zapisu -> dokument w MongoDB (osobny magazyn odczytu).
 - Działa asynchronicznie po commicie, więc odczyt "dogania" zapis (eventual consistency).
 - Jest idempotentna: zdarzenie dostarczone ponownie (at-least-once) nie zmieni wyniku,
   bo transakcja jest dopisywana tylko, jeśli jej id jeszcze nie ma w dokumencie.
 - Kolejność zdarzeń nie ma znaczenia: oba handlery tworzą dokument, jeśli go brakuje (upsert).
 */
@Component
class CardSummaryProjector {

    private final MongoTemplate mongoTemplate;

    CardSummaryProjector(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @ApplicationModuleListener
    void on(final CardAdded event) {
        mongoTemplate.upsert(byNumber(event.cardNumber()),
                new Update().set("expiration", event.expiration()).set("currencyCode", event.currencyCode()),
                CardSummaryDocument.class);
    }

    @ApplicationModuleListener
    void on(final TransactionAdded event) {
        ensureExists(event.cardNumber());
        var signedAmount = "PAYMENT".equals(event.type()) ? event.amount().negate() : event.amount();
        var entry = new CardSummaryDocument.TransactionEntry(event.transactionId(), event.timestamp().toInstant(), event.type(),
                event.amount(), event.currencyCode());
        var notYetProjected = byNumber(event.cardNumber()).addCriteria(Criteria.where("transactions.id").ne(event.transactionId()));
        mongoTemplate.updateFirst(notYetProjected,
                new Update().push("transactions", entry).inc("balance", new Decimal128(signedAmount)),
                CardSummaryDocument.class);
    }

    private void ensureExists(final String cardNumber) {
        mongoTemplate.findAndModify(byNumber(cardNumber), new Update().setOnInsert("balance", new Decimal128(java.math.BigDecimal.ZERO))
                .setOnInsert("transactions", java.util.List.of()), FindAndModifyOptions.options().upsert(true), CardSummaryDocument.class);
    }

    private Query byNumber(final String cardNumber) {
        return Query.query(Criteria.where("_id").is(cardNumber));
    }

}
