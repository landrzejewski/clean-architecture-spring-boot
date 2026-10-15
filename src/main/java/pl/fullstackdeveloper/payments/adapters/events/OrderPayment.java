package pl.fullstackdeveloper.payments.adapters.events;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/*
 W choreografii nikt nie powie płatnościom "zwróć 40 PLN na kartę X" - dostaną tylko fakt ShipmentFailed.
 Dlatego płatności same muszą pamiętać, czym opłacono zamówienie (lokalna kopia danych z OrderPlaced).
 */
@Entity
@Table(name = "order_payments")
class OrderPayment {

    @Id
    private String orderId;
    private String cardNumber;
    private BigDecimal amount;
    private String currencyCode;

    protected OrderPayment() {
    }

    OrderPayment(final String orderId, final String cardNumber, final BigDecimal amount, final String currencyCode) {
        this.orderId = orderId;
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    String getCardNumber() {
        return cardNumber;
    }

    BigDecimal getAmount() {
        return amount;
    }

    String getCurrencyCode() {
        return currencyCode;
    }

}
