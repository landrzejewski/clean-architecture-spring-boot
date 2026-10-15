package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.common.annotations.Retry;
import pl.fullstackdeveloper.common.annotations.Timer;
import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.CurrencyExchange;
import pl.fullstackdeveloper.payments.domain.DomainException;
import pl.fullstackdeveloper.payments.domain.Money;
import pl.fullstackdeveloper.payments.domain.Transaction;
import pl.fullstackdeveloper.payments.domain.TransactionId;
import pl.fullstackdeveloper.payments.domain.TransactionRegistered;

/*
 Use case orkiestruje: pobiera agregat przez port, deleguje logikę do domeny,
 zapisuje zmiany i publikuje zdarzenia. Sam nie zawiera reguł biznesowych.
 */
public class AddTransactionService implements AddTransactionUseCase {

    private final CurrencyExchange currencyExchange = new CurrencyExchange();
    private final DateTimeProvider dateTimeProvider;
    private final TransactionEventPublisher eventPublisher;
    private final CardRepository cardRepository;
    private final ExchangeRateProvider exchangeRateProvider;

    public AddTransactionService(final DateTimeProvider dateTimeProvider, final TransactionEventPublisher eventPublisher,
                                 final CardRepository cardRepository, final ExchangeRateProvider exchangeRateProvider) {
        this.dateTimeProvider = dateTimeProvider;
        this.eventPublisher = eventPublisher;
        this.cardRepository = cardRepository;
        this.exchangeRateProvider = exchangeRateProvider;
    }

    // konflikt współbieżnej modyfikacji karty -> ponów cały use case w nowej transakcji
    @Retry(on = CardModifiedConcurrentlyException.class)
    // odrzucenie transakcji to wynik biznesowy - nie psujemy transakcji wywołującego (np. listenera zdarzeń)
    @Atomic(noRollbackFor = {DomainException.class, CardNotFoundException.class})
    @Timer
    @Override
    public TransactionId handle(final AddTransactionCommand command) {
        var card = findCard(command);
        var value = toCardCurrency(command, card);
        var transaction = new Transaction(new TransactionId(), dateTimeProvider.getZonedDateTime(), value, command.type());
        card.registerTransaction(transaction);
        cardRepository.save(card);
        card.pullEvents().forEach(this::publish);
        return transaction.id();
    }

    private Card findCard(final AddTransactionCommand command) {
        return cardRepository.findByNumber(command.cardNumber())
                .orElseThrow(CardNotFoundException::new);
    }

    private Money toCardCurrency(final AddTransactionCommand command, final Card card) {
        if (command.value().hasCurrency(card.getCurrency())) {
            return command.value();
        }
        var rate = exchangeRateProvider.getRate(command.value().currency(), card.getCurrency());
        return currencyExchange.toCardCurrency(command.value(), rate, command.type());
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
