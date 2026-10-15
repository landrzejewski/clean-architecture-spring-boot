package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.CardEvent;
import pl.fullstackdeveloper.payments.domain.CardIssued;
import pl.fullstackdeveloper.payments.domain.TransactionRegistered;

// Tłumaczenie zdarzeń domenowych (wewnętrznych) na zdarzenia aplikacyjne (publikowane na zewnątrz)
public final class PaymentsEvents {

    public static PaymentsEvent from(final CardEvent event) {
        return switch (event) {
            case CardIssued issued -> new CardAdded(issued.cardNumber().value(), issued.expiration(), issued.currency().getCurrencyCode());
            case TransactionRegistered registered -> {
                var transaction = registered.transaction();
                yield new TransactionAdded(registered.cardNumber().value(), transaction.id().value().toString(), transaction.type().name(),
                        transaction.value().amount(), transaction.value().currency().getCurrencyCode(), transaction.timestamp());
            }
        };
    }

    private PaymentsEvents() {
    }

}
