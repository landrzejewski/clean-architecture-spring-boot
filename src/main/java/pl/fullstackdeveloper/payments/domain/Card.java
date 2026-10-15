package pl.fullstackdeveloper.payments.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;

/*
 Agregat w wersji Event Sourcing:
 - metoda biznesowa najpierw sprawdza niezmienniki, potem REJESTRUJE zdarzenie (record),
 - zmiana stanu dzieje się wyłącznie w apply(zdarzenie) - ta sama ścieżka przy obsłudze komendy
   i przy odtwarzaniu z historii (fromHistory),
 - agregat trzyma tylko stan potrzebny do podejmowania decyzji (saldo, waluta, ważność);
   pełna historia transakcji jest w zdarzeniach.
 */
public final class Card {

    private final List<CardEvent> pendingEvents = new ArrayList<>();
    private CardId id;
    private CardNumber number;
    private LocalDate expiration;
    private Currency currency;
    private Money balance;
    private long transactionsCount;
    private long version;

    private Card() {
    }

    public Card(final CardId id, final CardNumber number, final LocalDate expiration, final Currency currency) {
        record(new CardIssued(id, number, expiration, currency));
    }

    public static Card fromHistory(final List<? extends CardEvent> history) {
        if (history.isEmpty()) {
            throw new IllegalArgumentException("Card history cannot be empty");
        }
        var card = new Card();
        history.forEach(card::apply);
        return card;
    }

    public static Card fromSnapshot(final CardSnapshot snapshot, final List<? extends CardEvent> newerEvents) {
        var card = new Card();
        card.id = snapshot.id();
        card.number = snapshot.number();
        card.expiration = snapshot.expiration();
        card.currency = snapshot.currency();
        card.balance = snapshot.balance();
        card.transactionsCount = snapshot.transactionsCount();
        card.version = snapshot.version();
        newerEvents.forEach(card::apply);
        return card;
    }

    public CardSnapshot snapshot() {
        return new CardSnapshot(id, number, expiration, currency, balance, transactionsCount, version);
    }

    public void registerTransaction(final Transaction transaction) {
        validate(transaction);
        record(new TransactionRegistered(number, transaction));
    }

    private void validate(final Transaction transaction) {
        if (!transaction.isBefore(expiration)) {
            throw new InvalidTransactionTimestampException();
        }
        if (!transaction.hasCurrency(currency)) {
            throw new MismatchedCurrencyException();
        }
        if (transaction.type() != INFLOW && !balance.isGreaterOrEqual(transaction.value())) {
            throw new InsufficientBalanceException();
        }
    }

    private void record(final CardEvent event) {
        apply(event);
        pendingEvents.add(event);
    }

    // Jedyne miejsce zmiany stanu - bez walidacji: zdarzenie to fakt, który już zaszedł
    private void apply(final CardEvent event) {
        switch (event) {
            case CardIssued issued -> {
                id = issued.cardId();
                number = issued.cardNumber();
                expiration = issued.expiration();
                currency = issued.currency();
                balance = Money.zero(currency);
            }
            case TransactionRegistered registered -> {
                var transaction = registered.transaction();
                balance = switch (transaction.type()) {
                    case INFLOW -> balance.add(transaction.value());
                    case PAYMENT -> balance.subtract(transaction.value());
                };
                transactionsCount++;
            }
        }
        version++;
    }

    // Zdarzenia jeszcze niezapisane - do odczytu przez repozytorium (event store)
    public List<CardEvent> getPendingEvents() {
        return List.copyOf(pendingEvents);
    }

    // Zdarzenia od ostatniego odczytu - publikuje je warstwa aplikacji
    public List<CardEvent> pullEvents() {
        var pulledEvents = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return pulledEvents;
    }

    public CardId getId() {
        return id;
    }

    public CardNumber getNumber() {
        return number;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Money getBalance() {
        return balance;
    }

    public long getTransactionsCount() {
        return transactionsCount;
    }

    // Liczba zdarzeń zastosowanych do agregatu (łącznie z niezapisanymi)
    public long getVersion() {
        return version;
    }

}
