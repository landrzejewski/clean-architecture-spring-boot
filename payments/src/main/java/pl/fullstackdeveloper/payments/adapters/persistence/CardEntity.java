package pl.fullstackdeveloper.payments.adapters.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
 Model persystencji - kształtowany pod bazę danych, nie pod reguły biznesowe.
 Package-private: poza adapterem nikt go nie widzi.
 */
@Entity
@Table(name = "cards")
class CardEntity {

    @Id
    private String id;
    @Column(unique = true, nullable = false)
    private String number;
    private LocalDate expiration;
    private String currencyCode;
    // zdenormalizowane saldo - wygodne do zapytań, a jego zmiana podbija wersję przy każdej transakcji
    private BigDecimal balance;
    @Version
    private long version;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "card_id", nullable = false)
    @OrderBy("timestamp")
    private List<TransactionEntity> transactions = new ArrayList<>();

    protected CardEntity() {
    }

    CardEntity(final String id, final String number, final LocalDate expiration, final String currencyCode) {
        this.id = id;
        this.number = number;
        this.expiration = expiration;
        this.currencyCode = currencyCode;
    }

    String getId() {
        return id;
    }

    String getNumber() {
        return number;
    }

    LocalDate getExpiration() {
        return expiration;
    }

    String getCurrencyCode() {
        return currencyCode;
    }

    BigDecimal getBalance() {
        return balance;
    }

    void setBalance(final BigDecimal balance) {
        this.balance = balance;
    }

    long getVersion() {
        return version;
    }

    List<TransactionEntity> getTransactions() {
        return transactions;
    }

}
