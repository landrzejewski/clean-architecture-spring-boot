package pl.fullstackdeveloper.payments.adapters.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.CardModifiedConcurrentlyException;
import pl.fullstackdeveloper.payments.application.CardRepository;
import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.CardEvent;
import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.CardSnapshot;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/*
 Repozytorium oparte o Event Store - implementuje TEN SAM port co adaptery JPA/JDBC wcześniej.
 Zapis = dopisanie nowych zdarzeń ze sprawdzeniem oczekiwanej wersji. Odczyt = migawka + nowsze zdarzenia.
 */
@Adapter
@Transactional(propagation = Propagation.MANDATORY)
class EventSourcedCardRepository implements CardRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventSourcedCardRepository.class);

    private final JdbcClient jdbcClient;
    private final StoredEventMapper mapper;
    private final long snapshotFrequency;

    EventSourcedCardRepository(final JdbcClient jdbcClient, final StoredEventMapper mapper,
                               @Value("${payments.snapshot-frequency:10}") final long snapshotFrequency) {
        this.jdbcClient = jdbcClient;
        this.mapper = mapper;
        this.snapshotFrequency = snapshotFrequency;
    }

    @Override
    public Optional<Card> findByNumber(final CardNumber cardNumber) {
        var snapshot = loadSnapshot(cardNumber);
        var fromVersion = snapshot.map(CardSnapshot::version).orElse(0L);
        var events = loadEvents(cardNumber, fromVersion);
        if (snapshot.isPresent()) {
            return Optional.of(Card.fromSnapshot(snapshot.get(), events));
        }
        return events.isEmpty() ? Optional.empty() : Optional.of(Card.fromHistory(events));
    }

    @Override
    public Card save(final Card card) {
        var pending = card.getPendingEvents();
        var expectedVersion = card.getVersion() - pending.size();
        try {
            var version = expectedVersion;
            for (var event : pending) {
                append(card.getNumber(), ++version, event);
            }
        } catch (DuplicateKeyException exception) {
            throw new CardModifiedConcurrentlyException(exception);
        }
        if (card.getVersion() / snapshotFrequency > expectedVersion / snapshotFrequency) {
            saveSnapshot(card.snapshot());
        }
        return card;
    }

    private void append(final CardNumber cardNumber, final long version, final CardEvent event) {
        jdbcClient.sql("""
                        insert into card_events (stream_id, version, type, payload, occurred_at)
                        values (:streamId, :version, :type, :payload, :occurredAt)""")
                .param("streamId", cardNumber.value())
                .param("version", version)
                .param("type", mapper.typeOf(event))
                .param("payload", mapper.toPayload(event))
                .param("occurredAt", Timestamp.from(Instant.now()))
                .update();
    }

    private List<CardEvent> loadEvents(final CardNumber cardNumber, final long afterVersion) {
        return jdbcClient.sql("select type, payload from card_events where stream_id = :streamId and version > :version order by version")
                .param("streamId", cardNumber.value())
                .param("version", afterVersion)
                .query((rs, rowNum) -> mapper.toEvent(rs.getString("type"), rs.getString("payload")))
                .list();
    }

    private Optional<CardSnapshot> loadSnapshot(final CardNumber cardNumber) {
        return jdbcClient.sql("select payload from card_snapshots where stream_id = :streamId")
                .param("streamId", cardNumber.value())
                .query(String.class)
                .optional()
                .map(mapper::toSnapshot);
    }

    private void saveSnapshot(final CardSnapshot snapshot) {
        LOGGER.info("Snapshot of card {} at version {}", snapshot.number().value(), snapshot.version());
        var updated = jdbcClient.sql("update card_snapshots set version = :version, payload = :payload where stream_id = :streamId")
                .param("streamId", snapshot.number().value())
                .param("version", snapshot.version())
                .param("payload", mapper.toPayload(snapshot))
                .update();
        if (updated == 0) {
            jdbcClient.sql("insert into card_snapshots (stream_id, version, payload) values (:streamId, :version, :payload)")
                    .param("streamId", snapshot.number().value())
                    .param("version", snapshot.version())
                    .param("payload", mapper.toPayload(snapshot))
                    .update();
        }
    }

}
