package pl.fullstackdeveloper.payments.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
 Encja JPA pełni tu rolę "modelu domeny", DTO dla REST i struktury tabeli jednocześnie.
 Brak zachowań - wyłącznie gettery i settery (model anemiczny).
 */
@Entity
@Table(name = "cards")
public class CardEntity {

    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String number;
    private LocalDate expiration;
    private String currencyCode;
    private BigDecimal balance;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "card_id")
    private List<TransactionEntity> transactions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getExpiration() {
        return expiration;
    }

    public void setExpiration(LocalDate expiration) {
        this.expiration = expiration;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

}
