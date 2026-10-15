package pl.fullstackdeveloper.payments.adapters.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "transactions")
class TransactionEntity {

    @Id
    private String id;
    private ZonedDateTime timestamp;
    private BigDecimal amount;
    private String currencyCode;
    private String type;

    protected TransactionEntity() {
    }

    TransactionEntity(final String id, final ZonedDateTime timestamp, final BigDecimal amount, final String currencyCode, final String type) {
        this.id = id;
        this.timestamp = timestamp;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.type = type;
    }

    String getId() {
        return id;
    }

    ZonedDateTime getTimestamp() {
        return timestamp;
    }

    BigDecimal getAmount() {
        return amount;
    }

    String getCurrencyCode() {
        return currencyCode;
    }

    String getType() {
        return type;
    }

}
