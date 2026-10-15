package pl.fullstackdeveloper.payments.queries;

import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.ResultPage;
import pl.fullstackdeveloper.common.cqrs.Query;

public record GetCardsQuery(PageSpec pageSpec) implements Query<ResultPage<CardSummary>> {
}
