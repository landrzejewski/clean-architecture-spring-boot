package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.stereotype.Component;
import pl.fullstackdeveloper.common.ResultPage;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.Money;
import pl.fullstackdeveloper.payments.domain.TransactionType;
import pl.fullstackdeveloper.payments.queries.CardSummary;

import java.util.Currency;

// Mapowanie na granicy adaptera: DTO <-> komendy / modele wyjściowe
@Component
class CardRestMapper {

    AddCardCommand toCommand(final AddCardRequest request) {
        return new AddCardCommand(Currency.getInstance(request.currencyCode()));
    }

    AddTransactionCommand toCommand(final String cardNumber, final AddTransactionRequest request) {
        var value = new Money(request.amount(), Currency.getInstance(request.currencyCode()));
        return new AddTransactionCommand(new CardNumber(cardNumber), value, toDomain(request.type()));
    }

    private TransactionType toDomain(final TransactionTypeDto type) {
        return switch (type) {
            case IN -> TransactionType.INFLOW;
            case OUT -> TransactionType.PAYMENT;
        };
    }

    // Kontrakt REST pozostał bez zmian - zmieniło się tylko źródło danych (model odczytu zamiast agregatu)
    CardResponse toResponse(final CardSummary summary) {
        return new CardResponse(summary.number(), summary.expiration(), summary.balance(), summary.currencyCode(),
                (int) summary.transactionsCount());
    }

    PageResponse<CardResponse> toResponse(final ResultPage<CardSummary> page) {
        var content = page.content().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, page.pageSpec().index(), page.pageSpec().size(), page.totalPages());
    }

}
