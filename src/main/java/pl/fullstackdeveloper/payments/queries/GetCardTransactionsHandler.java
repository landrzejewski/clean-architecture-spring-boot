package pl.fullstackdeveloper.payments.queries;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.cqrs.QueryHandler;

import java.util.List;

// Nowy odczyt, którego strona zapisu nie musiała obsługiwać - bez ładowania agregatu z całą historią
@Component
@Transactional(readOnly = true)
class GetCardTransactionsHandler implements QueryHandler<GetCardTransactionsQuery, List<TransactionSummary>> {

    private final CardSummaryQueries queries;

    GetCardTransactionsHandler(final CardSummaryQueries queries) {
        this.queries = queries;
    }

    @Override
    public List<TransactionSummary> handle(final GetCardTransactionsQuery query) {
        return queries.jdbc().sql("""
                        select t.id, t.timestamp, t.type, t.amount, t.currency_code
                        from transactions t join cards c on c.id = t.card_id
                        where c.number = :number
                        order by t.timestamp desc""")
                .param("number", query.cardNumber())
                .query((rs, rowNum) -> new TransactionSummary(rs.getString("id"), rs.getTimestamp("timestamp").toInstant(),
                        rs.getString("type"), rs.getBigDecimal("amount"), rs.getString("currency_code")))
                .list();
    }

}
