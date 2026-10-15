package pl.fullstackdeveloper.compare.layered.business;

import pl.fullstackdeveloper.compare.layered.persistence.CardDao;
import pl.fullstackdeveloper.compare.layered.persistence.CardRecord;

import java.math.BigDecimal;

// Warstwa logiki zależy od warstwy persystencji (zależności "w dół")
public class CardService {

    private final CardDao cardDao;

    public CardService(CardDao cardDao) {
        this.cardDao = cardDao;
    }

    public BigDecimal topUp(String cardNumber, BigDecimal amount) {
        CardRecord card = cardDao.findByNumber(cardNumber);
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        card.setBalance(card.getBalance().add(amount));
        cardDao.update(card);
        return card.getBalance();
    }

}
