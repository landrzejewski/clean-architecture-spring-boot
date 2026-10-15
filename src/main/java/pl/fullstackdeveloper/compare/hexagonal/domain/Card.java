package pl.fullstackdeveloper.compare.hexagonal.domain;

import java.math.BigDecimal;

public class Card {

    private final String number;
    private BigDecimal balance;

    public Card(String number, BigDecimal balance) {
        this.number = number;
        this.balance = balance;
    }

    public void topUp(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance = balance.add(amount);
    }

    public String getNumber() {
        return number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

}
