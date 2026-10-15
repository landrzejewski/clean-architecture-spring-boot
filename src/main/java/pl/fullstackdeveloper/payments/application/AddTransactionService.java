package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.Transaction;
import pl.fullstackdeveloper.payments.domain.TransactionId;
import pl.fullstackdeveloper.payments.domain.TransactionRegistered;

/*
 Use case orkiestruje: pobiera agregat przez port, deleguje logikę do domeny,
 zapisuje zmiany i publikuje zdarzenia. Sam nie zawiera reguł biznesowych.
 */
public class AddTransactionService implements AddTransactionUseCase {

    private final DateTimeProvider dateTimeProvider;
    private final TransactionEventPublisher eventPublisher;
    private final CardRepository cardRepository;

    public AddTransactionService(final DateTimeProvider dateTimeProvider, final TransactionEventPublisher eventPublisher,
                                 final CardRepository cardRepository) {
        this.dateTimeProvider = dateTimeProvider;
        this.eventPublisher = eventPublisher;
        this.cardRepository = cardRepository;
    }

    @Override
    public TransactionId handle(final AddTransactionCommand command) {
        var card = findCard(command);
        var transaction = new Transaction(new TransactionId(), dateTimeProvider.getZonedDateTime(), command.value(), command.type());
        card.registerTransaction(transaction);
        cardRepository.save(card);
        card.pullEvents().forEach(this::publish);
        return transaction.id();
    }

    private Card findCard(final AddTransactionCommand command) {
        return cardRepository.findByNumber(command.cardNumber())
                .orElseThrow(CardNotFoundException::new);
    }

    private void publish(final TransactionRegistered event) {
        var transaction = event.transaction();
        eventPublisher.publish(new TransactionAdded(
                event.cardNumber().value(),
                transaction.id().value().toString(),
                transaction.type().name(),
                transaction.value().amount(),
                transaction.value().currency().getCurrencyCode(),
                transaction.timestamp()
        ));
    }

}
