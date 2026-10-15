package pl.fullstackdeveloper.payments.queries;

import pl.fullstackdeveloper.common.cqrs.Query;

import java.util.List;

public record GetCardTransactionsQuery(String cardNumber) implements Query<List<TransactionSummary>> {
}
