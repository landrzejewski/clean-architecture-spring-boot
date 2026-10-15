package pl.fullstackdeveloper.payments.queries;

import pl.fullstackdeveloper.common.cqrs.Query;

import java.util.Optional;

public record GetCardSummaryQuery(String cardNumber) implements Query<Optional<CardSummary>> {
}
