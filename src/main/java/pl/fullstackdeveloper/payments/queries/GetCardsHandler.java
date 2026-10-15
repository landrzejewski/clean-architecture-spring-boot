package pl.fullstackdeveloper.payments.queries;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.common.ResultPage;
import pl.fullstackdeveloper.common.cqrs.QueryHandler;

@Component
class GetCardsHandler implements QueryHandler<GetCardsQuery, ResultPage<CardSummary>> {

    private final MongoTemplate mongoTemplate;

    GetCardsHandler(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ResultPage<CardSummary> handle(final GetCardsQuery query) {
        var pageSpec = query.pageSpec();
        var page = new Query().with(Sort.by("_id")).skip((long) pageSpec.index() * pageSpec.size()).limit(pageSpec.size());
        var cards = mongoTemplate.find(page, CardSummaryDocument.class).stream().map(CardSummaryDocument::toSummary).toList();
        var total = mongoTemplate.count(new Query(), CardSummaryDocument.class);
        return new ResultPage<>(cards, pageSpec, (int) Math.ceil((double) total / pageSpec.size()));
    }

}
