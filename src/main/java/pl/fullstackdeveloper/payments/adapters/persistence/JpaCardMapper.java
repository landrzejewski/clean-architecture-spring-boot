package pl.fullstackdeveloper.payments.adapters.persistence;

import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.payments.domain.*;

import java.time.ZoneId;
import java.util.Currency;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 Mapowanie dwukierunkowe (two-way mapping): model domeny <-> model persystencji.
 Agregat odtwarzamy przez Card.restore() - bez ponownej walidacji i bez zdarzeń.
 */
@Component
class JpaCardMapper {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    Card toDomain(final CardEntity entity) {
        var currency = Currency.getInstance(entity.getCurrencyCode());
        var transactions = entity.getTransactions().stream()
                .map(transaction -> toDomain(transaction))
                .toList();
        return Card.restore(new CardId(UUID.fromString(entity.getId())), new CardNumber(entity.getNumber()),
                entity.getExpiration(), currency, transactions);
    }

    private Transaction toDomain(final TransactionEntity entity) {
        var value = new Money(entity.getAmount(), Currency.getInstance(entity.getCurrencyCode()));
        return new Transaction(new TransactionId(UUID.fromString(entity.getId())), entity.getTimestamp().withZoneSameInstant(ZONE),
                value, TransactionType.valueOf(entity.getType()));
    }

    CardEntity toNewEntity(final Card card) {
        var entity = new CardEntity(card.getId().value().toString(), card.getNumber().value(), card.getExpiration(),
                card.getCurrency().getCurrencyCode());
        update(entity, card);
        return entity;
    }

    // Aktualizacja zarządzanej encji - Hibernate wykryje zmiany i sprawdzi @Version
    void update(final CardEntity entity, final Card card) {
        entity.setBalance(card.getBalance().amount());
        Set<String> persistedIds = entity.getTransactions().stream()
                .map(TransactionEntity::getId)
                .collect(Collectors.toSet());
        card.getTransactions().stream()
                .filter(transaction -> !persistedIds.contains(transaction.id().value().toString()))
                .map(this::toEntity)
                .forEach(entity.getTransactions()::add);
    }

    private TransactionEntity toEntity(final Transaction transaction) {
        var value = transaction.value();
        return new TransactionEntity(transaction.id().value().toString(), transaction.timestamp(), value.amount(),
                value.currency().getCurrencyCode(), transaction.type().name());
    }

}
