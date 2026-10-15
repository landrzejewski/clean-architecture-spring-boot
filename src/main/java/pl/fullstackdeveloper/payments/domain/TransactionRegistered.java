package pl.fullstackdeveloper.payments.domain;

/*
 Zdarzenie domenowe - fakt biznesowy, który już zaszedł (czas przeszły w nazwie).
 */
public record TransactionRegistered(CardNumber cardNumber, Transaction transaction) {
}
