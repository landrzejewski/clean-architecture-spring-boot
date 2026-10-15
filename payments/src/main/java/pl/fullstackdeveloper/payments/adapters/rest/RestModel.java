package pl.fullstackdeveloper.payments.adapters.rest;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
 Kontrakt REST (DTO) - celowo oddzielony od modelu domeny i modelu wyjściowego use case'ów.
 Zmiana domeny nie zmienia JSON-a i odwrotnie.
 */
record AddCardRequest(@NotNull @Pattern(regexp = "[A-Z]{3}") String currencyCode) {
}

record AddTransactionRequest(@NotNull @Positive BigDecimal amount,
                             @NotNull @Pattern(regexp = "[A-Z]{3}") String currencyCode,
                             @NotNull TransactionTypeDto type) {
}

enum TransactionTypeDto {

    IN, OUT

}

record CardNumberResponse(String number) {
}

record TransactionIdResponse(String id) {
}

record CardResponse(String number, LocalDate expiration, BigDecimal balance, String currencyCode, int transactionsCount) {
}

record PageResponse<T>(List<T> content, int pageIndex, int pageSize, int totalPages) {
}
