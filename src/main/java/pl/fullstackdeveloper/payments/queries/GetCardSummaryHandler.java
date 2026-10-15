package pl.fullstackdeveloper.payments.queries;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.cqrs.QueryHandler;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
class GetCardSummaryHandler implements QueryHandler<GetCardSummaryQuery, Optional<CardSummary>> {

    private final CardSummaryQueries queries;

    GetCardSummaryHandler(final CardSummaryQueries queries) {
        this.queries = queries;
    }

    @Override
    public Optional<CardSummary> handle(final GetCardSummaryQuery query) {
        return queries.jdbc().sql(CardSummaryQueries.CARD_SUMMARY_SQL + " where c.number = :number")
                .param("number", query.cardNumber())
                .query(CardSummaryQueries::toCardSummary)
                .optional();
    }

}
