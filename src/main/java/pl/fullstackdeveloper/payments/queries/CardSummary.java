package pl.fullstackdeveloper.payments.queries;

import java.math.BigDecimal;
import java.time.LocalDate;

// Model odczytu - płaski, skrojony pod ekran / API, budowany bezpośrednio z bazy (bez agregatu)
public record CardSummary(String number, LocalDate expiration, BigDecimal balance, String currencyCode, long transactionsCount) {
}
