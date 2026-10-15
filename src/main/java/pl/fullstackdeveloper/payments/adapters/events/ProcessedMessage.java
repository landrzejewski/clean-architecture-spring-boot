package pl.fullstackdeveloper.payments.adapters.events;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 Rejestr przetworzonych komunikatów (idempotent receiver). Komunikaty w sadze są dostarczane
 co najmniej raz - ponowne dostarczenie nie może obciążyć karty drugi raz. Zapamiętujemy wynik
 i przy duplikacie odsyłamy dokładnie tę samą odpowiedź.
 */
@Entity
@Table(name = "processed_messages")
class ProcessedMessage {

    @Id
    private String messageKey;
    private boolean completed;
    private String paymentId;
    private String reason;

    protected ProcessedMessage() {
    }

    ProcessedMessage(final String messageKey, final boolean completed, final String paymentId, final String reason) {
        this.messageKey = messageKey;
        this.completed = completed;
        this.paymentId = paymentId;
        this.reason = reason;
    }

    boolean isCompleted() {
        return completed;
    }

    String getPaymentId() {
        return paymentId;
    }

    String getReason() {
        return reason;
    }

}
