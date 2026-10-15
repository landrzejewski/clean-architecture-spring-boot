package pl.fullstackdeveloper.compare.layered.persistence;

import java.math.BigDecimal;

public class CardRecord {

    private String number;
    private BigDecimal balance;

    public CardRecord(String number, BigDecimal balance) {
        this.number = number;
        this.balance = balance;
    }

    public String getNumber() {
        return number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}
