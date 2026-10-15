package pl.fullstackdeveloper.payments.queries;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.common.cqrs.QueryHandler;

import java.util.Comparator;
import java.util.List;

@Component
class GetCardTransactionsHandler implements QueryHandler<GetCardTransactionsQuery, List<TransactionSummary>> {

    private final MongoTemplate mongoTemplate;

    GetCardTransactionsHandler(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<TransactionSummary> handle(final GetCardTransactionsQuery query) {
        var document = mongoTemplate.findById(query.cardNumber(), CardSummaryDocument.class);
        if (document == null) {
            return List.of();
        }
        return document.getTransactions().stream()
                .sorted(Comparator.comparing(CardSummaryDocument.TransactionEntry::timestamp).reversed())
                .map(entry -> new TransactionSummary(entry.id(), entry.timestamp(), entry.type(), entry.amount(), entry.currencyCode()))
                .toList();
    }

}
