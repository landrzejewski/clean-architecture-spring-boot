package pl.fullstackdeveloper.payments.adapters.persistence;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.PaymentsEvent;
import pl.fullstackdeveloper.payments.application.PaymentsEventHistory;
import pl.fullstackdeveloper.payments.application.PaymentsEvents;

import java.util.function.Consumer;

// Odtworzenie wszystkich zdarzeń w kolejności zapisu (kolumna position) - podstawa replay projekcji
@Adapter
class EventStoreHistory implements PaymentsEventHistory {

    private final JdbcClient jdbcClient;
    private final StoredEventMapper mapper;

    EventStoreHistory(final JdbcClient jdbcClient, final StoredEventMapper mapper) {
        this.jdbcClient = jdbcClient;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public void replay(final Consumer<PaymentsEvent> consumer) {
        jdbcClient.sql("select type, payload from card_events order by position")
                .query((rs, rowNum) -> mapper.toEvent(rs.getString("type"), rs.getString("payload")))
                .stream()
                .map(PaymentsEvents::from)
                .forEach(consumer);
    }

}
