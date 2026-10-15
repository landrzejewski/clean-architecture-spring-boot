package pl.fullstackdeveloper.payments.domain;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Currency;

public final class CardTestFixtures {

    public static final Currency PLN = Currency.getInstance("PLN");
    public static final Currency EUR = Currency.getInstance("EUR");
    public static final CardNumber CARD_NUMBER = new CardNumber("1234567890123456");
    public static final LocalDate EXPIRATION = LocalDate.of(2030, 1, 1);
    public static final ZonedDateTime BEFORE_EXPIRATION = ZonedDateTime.of(2029, 6, 1, 12, 0, 0, 0, ZoneId.of("Europe/Warsaw"));
    public static final ZonedDateTime AFTER_EXPIRATION = ZonedDateTime.of(2030, 6, 1, 12, 0, 0, 0, ZoneId.of("Europe/Warsaw"));

    public static Card plnCard() {
        return new Card(new CardId(), CARD_NUMBER, EXPIRATION, PLN);
    }

    public static Transaction inflow(final String amount) {
        return transaction(amount, PLN, TransactionType.INFLOW, BEFORE_EXPIRATION);
    }

    public static Transaction payment(final String amount) {
        return transaction(amount, PLN, TransactionType.PAYMENT, BEFORE_EXPIRATION);
    }

    public static Transaction transaction(final String amount, final Currency currency, final TransactionType type, final ZonedDateTime timestamp) {
        return new Transaction(new TransactionId(), timestamp, new Money(new java.math.BigDecimal(amount), currency), type);
    }

    private CardTestFixtures() {
    }

}
