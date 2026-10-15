package pl.fullstackdeveloper.payments.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.fullstackdeveloper.payments.model.CardEntity;
import pl.fullstackdeveloper.payments.model.TransactionEntity;
import pl.fullstackdeveloper.payments.repository.CardRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

/*
 Transaction Script: cała logika biznesowa w serwisie, operującym bezpośrednio na encjach JPA.
 Problemy:
 - reguły (saldo, waluta, data ważności) rozproszone w if-ach, łatwo je ominąć ustawiając pola encji z innego miejsca
 - logika zależy od JPA, Springa, zegara systemowego i losowości - trudno ją testować bez kontekstu Springa
 - serwis rośnie z każdą nową funkcją (np. prowizje, limity, wiele walut)
 */
@Service
@Transactional
public class CardService {

    private static final int CARD_NUMBER_LENGTH = 16;
    private static final int CARD_VALIDITY_IN_YEARS = 1;

    private final CardRepository cardRepository;
    private final Random random = new Random();

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CardEntity addCard(String currencyCode) {
        var card = new CardEntity();
        card.setNumber(generateNumber());
        card.setCurrencyCode(currencyCode);
        card.setExpiration(LocalDate.now().plusYears(CARD_VALIDITY_IN_YEARS));
        card.setBalance(BigDecimal.ZERO);
        return cardRepository.save(card);
    }

    public CardEntity addTransaction(String cardNumber, BigDecimal amount, String currencyCode, String type) {
        var card = cardRepository.findByNumber(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (!card.getCurrencyCode().equals(currencyCode)) {
            throw new IllegalStateException("Mismatched currency");
        }
        var now = ZonedDateTime.now();
        if (!now.toLocalDate().isBefore(card.getExpiration())) {
            throw new IllegalStateException("Card expired");
        }
        if (type.equals("OUT")) {
            if (card.getBalance().compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient balance");
            }
            card.setBalance(card.getBalance().subtract(amount));
        } else if (type.equals("IN")) {
            card.setBalance(card.getBalance().add(amount));
        } else {
            throw new IllegalArgumentException("Unknown transaction type");
        }
        var transaction = new TransactionEntity();
        transaction.setTimestamp(now);
        transaction.setAmount(amount);
        transaction.setCurrencyCode(currencyCode);
        transaction.setType(type);
        card.getTransactions().add(transaction);
        System.out.println("New " + type + " transaction on card " + cardNumber);
        return card;
    }

    @Transactional(readOnly = true)
    public List<CardEntity> getCards() {
        return cardRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CardEntity getCard(String number) {
        return cardRepository.findByNumber(number)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));
    }

    private String generateNumber() {
        var number = new StringBuilder();
        for (int i = 0; i < CARD_NUMBER_LENGTH; i++) {
            number.append(random.nextInt(10));
        }
        return number.toString();
    }

}
