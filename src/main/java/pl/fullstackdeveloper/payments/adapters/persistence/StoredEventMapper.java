package pl.fullstackdeveloper.payments.adapters.persistence;

import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.payments.domain.*;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.UUID;

/*
 Format zapisu zdarzeń jest kontraktem z przeszłością - zdarzenia zapisane rok temu muszą dać się
 odczytać dziś. Dlatego nie serializujemy klas domenowych wprost, tylko jawne, płaskie "payloady"
 (zmiana domeny nie zmienia formatu; zmianę formatu obsługujemy tu - np. upcasting starych wersji).
 */
@Component
class StoredEventMapper {

    record CardIssuedV1(String cardId, String number, LocalDate expiration, String currency) {
    }

    record TransactionRegisteredV1(String number, String transactionId, ZonedDateTime timestamp, BigDecimal amount,
                                   String currency, String type) {
    }

    record CardSnapshotV1(String cardId, String number, LocalDate expiration, String currency, BigDecimal balance,
                          long transactionsCount, long version) {
    }

    private final JsonMapper jsonMapper;

    StoredEventMapper(final JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    String typeOf(final CardEvent event) {
        return switch (event) {
            case CardIssued issued -> "CardIssued.v1";
            case TransactionRegistered registered -> "TransactionRegistered.v1";
        };
    }

    String toPayload(final CardEvent event) {
        return jsonMapper.writeValueAsString(switch (event) {
            case CardIssued issued -> new CardIssuedV1(issued.cardId().value().toString(), issued.cardNumber().value(),
                    issued.expiration(), issued.currency().getCurrencyCode());
            case TransactionRegistered registered -> {
                var transaction = registered.transaction();
                yield new TransactionRegisteredV1(registered.cardNumber().value(), transaction.id().value().toString(), transaction.timestamp(),
                        transaction.value().amount(), transaction.value().currency().getCurrencyCode(), transaction.type().name());
            }
        });
    }

    CardEvent toEvent(final String type, final String payload) {
        return switch (type) {
            case "CardIssued.v1" -> {
                var stored = jsonMapper.readValue(payload, CardIssuedV1.class);
                yield new CardIssued(new CardId(UUID.fromString(stored.cardId())), new CardNumber(stored.number()), stored.expiration(),
                        Currency.getInstance(stored.currency()));
            }
            case "TransactionRegistered.v1" -> {
                var stored = jsonMapper.readValue(payload, TransactionRegisteredV1.class);
                var transaction = new Transaction(new TransactionId(UUID.fromString(stored.transactionId())), stored.timestamp(),
                        new Money(stored.amount(), Currency.getInstance(stored.currency())), TransactionType.valueOf(stored.type()));
                yield new TransactionRegistered(new CardNumber(stored.number()), transaction);
            }
            default -> throw new IllegalStateException("Unknown event type " + type);
        };
    }

    String toPayload(final CardSnapshot snapshot) {
        return jsonMapper.writeValueAsString(new CardSnapshotV1(snapshot.id().value().toString(), snapshot.number().value(),
                snapshot.expiration(), snapshot.currency().getCurrencyCode(), snapshot.balance().amount(),
                snapshot.transactionsCount(), snapshot.version()));
    }

    CardSnapshot toSnapshot(final String payload) {
        var stored = jsonMapper.readValue(payload, CardSnapshotV1.class);
        var currency = Currency.getInstance(stored.currency());
        return new CardSnapshot(new CardId(UUID.fromString(stored.cardId())), new CardNumber(stored.number()), stored.expiration(),
                currency, new Money(stored.balance(), currency), stored.transactionsCount(), stored.version());
    }

}
