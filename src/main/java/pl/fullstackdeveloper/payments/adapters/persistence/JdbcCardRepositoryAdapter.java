package pl.fullstackdeveloper.payments.adapters.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.ResultPage;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.domain.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.*;

/*
 Druga implementacja tego samego portu - czysty SQL przez JdbcClient (profil "jdbc").
 Inna strategia współbieżności: blokada pesymistyczna (SELECT ... FOR UPDATE) przy odczycie
 zamiast @Version. Use case'y, domena i REST nie zmieniły się ani o linijkę.
 */
@Adapter
@Profile("jdbc")
@Transactional(propagation = Propagation.MANDATORY)
class JdbcCardRepositoryAdapter implements CardRepository {

    private final JdbcClient jdbcClient;

    JdbcCardRepositoryAdapter(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Card> findByNumber(final CardNumber cardNumber) {
        return jdbcClient.sql("select * from cards where number = :number for update")
                .param("number", cardNumber.value())
                .query(this::toCardRow)
                .optional()
                .map(this::toDomain);
    }

    @Override
    public ResultPage<Card> findAll(final PageSpec pageSpec) {
        var cards = jdbcClient.sql("select * from cards order by number limit :limit offset :offset")
                .param("limit", pageSpec.size())
                .param("offset", pageSpec.index() * pageSpec.size())
                .query(this::toCardRow)
                .list()
                .stream()
                .map(this::toDomain)
                .toList();
        var total = jdbcClient.sql("select count(*) from cards").query(Long.class).single();
        return new ResultPage<>(cards, pageSpec, (int) Math.ceil((double) total / pageSpec.size()));
    }

    @Override
    public Card save(final Card card) {
        var id = card.getId().value().toString();
        var updated = jdbcClient.sql("update cards set balance = :balance, version = version + 1 where id = :id")
                .param("balance", card.getBalance().amount())
                .param("id", id)
                .update();
        if (updated == 0) {
            jdbcClient.sql("""
                            insert into cards (id, number, expiration, currency_code, balance, version)
                            values (:id, :number, :expiration, :currency, :balance, 0)""")
                    .param("id", id)
                    .param("number", card.getNumber().value())
                    .param("expiration", card.getExpiration())
                    .param("currency", card.getCurrency().getCurrencyCode())
                    .param("balance", card.getBalance().amount())
                    .update();
        }
        insertNewTransactions(id, card.getTransactions());
        return card;
    }

    private void insertNewTransactions(final String cardId, final List<Transaction> transactions) {
        var persisted = new HashSet<>(jdbcClient.sql("select id from transactions where card_id = :cardId")
                .param("cardId", cardId)
                .query(String.class)
                .list());
        transactions.stream()
                .filter(transaction -> !persisted.contains(transaction.id().value().toString()))
                .forEach(transaction -> jdbcClient.sql("""
                                insert into transactions (id, card_id, timestamp, amount, currency_code, type)
                                values (:id, :cardId, :timestamp, :amount, :currency, :type)""")
                        .param("id", transaction.id().value().toString())
                        .param("cardId", cardId)
                        .param("timestamp", Timestamp.from(transaction.timestamp().toInstant()))
                        .param("amount", transaction.value().amount())
                        .param("currency", transaction.value().currency().getCurrencyCode())
                        .param("type", transaction.type().name())
                        .update());
    }

    private Card toDomain(final CardRow row) {
        var transactions = jdbcClient.sql("select * from transactions where card_id = :cardId order by timestamp")
                .param("cardId", row.id())
                .query((rs, rowNum) -> new Transaction(
                        new TransactionId(UUID.fromString(rs.getString("id"))),
                        rs.getTimestamp("timestamp").toInstant().atZone(ZoneId.systemDefault()),
                        new Money(rs.getBigDecimal("amount"), Currency.getInstance(rs.getString("currency_code"))),
                        TransactionType.valueOf(rs.getString("type"))))
                .list();
        return Card.restore(new CardId(UUID.fromString(row.id())), new CardNumber(row.number()), row.expiration(),
                Currency.getInstance(row.currencyCode()), transactions);
    }

    private CardRow toCardRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new CardRow(rs.getString("id"), rs.getString("number"), rs.getDate("expiration").toLocalDate(), rs.getString("currency_code"));
    }

    private record CardRow(String id, String number, java.time.LocalDate expiration, String currencyCode) {
    }

}
