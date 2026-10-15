package pl.fullstackdeveloper.payments.application;

import java.time.LocalDate;

public record CardAdded(String cardNumber, LocalDate expiration, String currencyCode) implements PaymentsEvent {
}
