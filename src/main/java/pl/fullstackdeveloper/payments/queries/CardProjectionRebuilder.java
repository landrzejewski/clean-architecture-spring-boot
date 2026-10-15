package pl.fullstackdeveloper.payments.queries;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/*
 Model odczytu jest "pochodną" - można go w każdej chwili skasować i odbudować ze źródła prawdy
 (tu: tabele strony zapisu). Przydaje się po zmianie kształtu widoku albo po błędzie projekcji.
 */
@Component
public class CardProjectionRebuilder {

    private final JdbcClient jdbcClient;
    private final MongoTemplate mongoTemplate;

    CardProjectionRebuilder(final JdbcClient jdbcClient, final MongoTemplate mongoTemplate) {
        this.jdbcClient = jdbcClient;
        this.mongoTemplate = mongoTemplate;
    }

    @Transactional(readOnly = true)
    public long rebuild() {
        mongoTemplate.dropCollection(CardSummaryDocument.class);
        var cards = jdbcClient.sql("select id, number, expiration, currency_code from cards")
                .query((rs, rowNum) -> new Object[]{rs.getString("id"),
                        new CardSummaryDocument(rs.getString("number"), rs.getDate("expiration").toLocalDate(), rs.getString("currency_code"))})
                .list();
        for (var row : cards) {
            var document = (CardSummaryDocument) row[1];
            jdbcClient.sql("select id, timestamp, type, amount, currency_code from transactions where card_id = :cardId order by timestamp")
                    .param("cardId", row[0])
                    .query((rs, rowNum) -> new CardSummaryDocument.TransactionEntry(rs.getString("id"), rs.getTimestamp("timestamp").toInstant(),
                            rs.getString("type"), rs.getBigDecimal("amount"), rs.getString("currency_code")))
                    .list()
                    .forEach(entry -> {
                        document.getTransactions().add(entry);
                        var signed = "PAYMENT".equals(entry.type()) ? entry.amount().negate() : entry.amount();
                        document.setBalance(document.getBalance().add(signed));
                    });
            mongoTemplate.save(document);
        }
        return cards.size();
    }

}
