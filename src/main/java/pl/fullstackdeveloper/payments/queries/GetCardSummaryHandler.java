package pl.fullstackdeveloper.payments.queries;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.common.cqrs.QueryHandler;

import java.util.Optional;

@Component
class GetCardSummaryHandler implements QueryHandler<GetCardSummaryQuery, Optional<CardSummary>> {

    private final MongoTemplate mongoTemplate;

    GetCardSummaryHandler(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<CardSummary> handle(final GetCardSummaryQuery query) {
        return Optional.ofNullable(mongoTemplate.findById(query.cardNumber(), CardSummaryDocument.class))
                .map(CardSummaryDocument::toSummary);
    }

}
