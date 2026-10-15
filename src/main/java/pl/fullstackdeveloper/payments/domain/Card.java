package pl.fullstackdeveloper.payments.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;

/*
 Agregat - granica spójności. Stan (saldo) zmienia się wyłącznie przez metody biznesowe,
 które pilnują niezmienników: waluta, data ważności, wystarczające środki.
 Brak setterów, brak zależności od frameworków.
 */
public final class Card {

    private final CardId id;
    private final CardNumber number;
    private final LocalDate expiration;
    private final Currency currency;
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<TransactionRegistered> events = new ArrayList<>();
    private Money balance;

    public Card(final CardId id, final CardNumber number, final LocalDate expiration, final Currency currency) {
        this.id = id;
        this.number = number;
        this.expiration = expiration;
        this.currency = currency;
        this.balance = Money.zero(currency);
    }

    // Odtworzenie agregatu z zapisanego stanu - bez walidacji i bez publikowania zdarzeń
    public static Card restore(final CardId id, final CardNumber number, final LocalDate expiration,
                               final Currency currency, final List<Transaction> transactions) {
        var card = new Card(id, number, expiration, currency);
        transactions.forEach(card::commit);
        return card;
    }

    public void registerTransaction(final Transaction transaction) {
        validate(transaction);
        commit(transaction);
        events.add(new TransactionRegistered(number, transaction));
    }

    private void validate(final Transaction transaction) {
        if (!transaction.isBefore(expiration)) {
            throw new InvalidTransactionTimestampException();
        }
        if (!transaction.hasCurrency(currency)) {
            throw new MismatchedCurrencyException();
        }
        if (!hasSufficientBalanceFor(transaction)) {
            throw new InsufficientBalanceException();
        }
    }

    private boolean hasSufficientBalanceFor(final Transaction transaction) {
        return transaction.type() == INFLOW || balance.isGreaterOrEqual(transaction.value());
    }

    private void commit(final Transaction transaction) {
        transactions.add(transaction);
        balance = switch (transaction.type()) {
            case INFLOW -> balance.add(transaction.value());
            case PAYMENT -> balance.subtract(transaction.value());
        };
    }

    // Zdarzenia zebrane od ostatniego odczytu - publikuje je warstwa aplikacji
    public List<TransactionRegistered> pullEvents() {
        var pulledEvents = List.copyOf(events);
        events.clear();
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

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }

}
