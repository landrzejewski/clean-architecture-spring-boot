package pl.fullstackdeveloper.payments.queries;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/*
 Wspólny fragment SQL strony odczytu. Strona zapytań nie zna agregatu Card ani portu CardRepository:
 czyta tabele bezpośrednio i liczy to, czego potrzebuje widok (np. liczbę transakcji) po stronie bazy.
 */
@Component
class CardSummaryQueries {

    static final String CARD_SUMMARY_SQL = """
            select c.number, c.expiration, c.balance, c.currency_code,
                   (select count(*) from transactions t where t.card_id = c.id) as transactions_count
            from cards c
            """;

    private final JdbcClient jdbcClient;

    CardSummaryQueries(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    JdbcClient jdbc() {
        return jdbcClient;
    }

    static CardSummary toCardSummary(final ResultSet rs, final int rowNum) throws SQLException {
        return new CardSummary(rs.getString("number"), rs.getDate("expiration").toLocalDate(), rs.getBigDecimal("balance"),
                rs.getString("currency_code"), rs.getLong("transactions_count"));
    }

}
