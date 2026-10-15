package pl.fullstackdeveloper.payments.queries;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
 Dokument modelu odczytu - zdenormalizowany "gotowy widok" karty: saldo policzone z góry,
 transakcje osadzone w dokumencie. Jeden odczyt = jeden dokument, bez złączeń i bez agregatu.
 */
@Document("card_summaries")
class CardSummaryDocument {

    @Id
    private String number;
    private LocalDate expiration;
    private String currencyCode;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal balance = BigDecimal.ZERO;
    private List<TransactionEntry> transactions = new ArrayList<>();

    record TransactionEntry(String id, Instant timestamp, String type,
                            @Field(targetType = FieldType.DECIMAL128) BigDecimal amount, String currencyCode) {
    }

    CardSummaryDocument() {
    }

    CardSummaryDocument(final String number, final LocalDate expiration, final String currencyCode) {
        this.number = number;
        this.expiration = expiration;
        this.currencyCode = currencyCode;
    }

    CardSummary toSummary() {
        return new CardSummary(number, expiration, balance, currencyCode, transactions.size());
    }

    String getNumber() {
        return number;
    }

    BigDecimal getBalance() {
        return balance;
    }

    void setBalance(final BigDecimal balance) {
        this.balance = balance;
    }

    List<TransactionEntry> getTransactions() {
        return transactions;
    }

}
