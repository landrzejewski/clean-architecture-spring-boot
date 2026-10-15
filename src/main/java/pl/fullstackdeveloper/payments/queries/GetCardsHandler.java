package pl.fullstackdeveloper.payments.queries;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.ResultPage;
import pl.fullstackdeveloper.common.cqrs.QueryHandler;

@Component
@Transactional(readOnly = true)
class GetCardsHandler implements QueryHandler<GetCardsQuery, ResultPage<CardSummary>> {

    private final CardSummaryQueries queries;

    GetCardsHandler(final CardSummaryQueries queries) {
        this.queries = queries;
    }

    @Override
    public ResultPage<CardSummary> handle(final GetCardsQuery query) {
        var pageSpec = query.pageSpec();
        var cards = queries.jdbc().sql(CardSummaryQueries.CARD_SUMMARY_SQL + " order by c.number limit :limit offset :offset")
                .param("limit", pageSpec.size())
                .param("offset", pageSpec.index() * pageSpec.size())
                .query(CardSummaryQueries::toCardSummary)
                .list();
        var total = queries.jdbc().sql("select count(*) from cards").query(Long.class).single();
        return new ResultPage<>(cards, pageSpec, (int) Math.ceil((double) total / pageSpec.size()));
    }

}
