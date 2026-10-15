package pl.fullstackdeveloper.payments.queries;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionSummary(String id, Instant timestamp, String type, BigDecimal amount, String currencyCode) {
}
