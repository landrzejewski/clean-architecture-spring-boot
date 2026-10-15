package pl.fullstackdeveloper.payments.adapters.api;

import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.api.PaymentRequest;
import pl.fullstackdeveloper.payments.api.PaymentResult;
import pl.fullstackdeveloper.payments.api.PaymentsApi;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionUseCase;
import pl.fullstackdeveloper.payments.application.CardNotFoundException;
import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.DomainException;
import pl.fullstackdeveloper.payments.domain.Money;
import pl.fullstackdeveloper.payments.domain.TransactionType;

import java.util.Currency;

/*
 Adapter wejściowy dla innych modułów (in-process). Tłumaczy kontrakt API na komendę use case'u,
 a wyjątki domenowe na wynik PaymentResult.Rejected. Package-private - nie da się go użyć bezpośrednio.
 */
@Adapter
class PaymentsApiAdapter implements PaymentsApi {

    private final AddTransactionUseCase addTransactionUseCase;

    PaymentsApiAdapter(final AddTransactionUseCase addTransactionUseCase) {
        this.addTransactionUseCase = addTransactionUseCase;
    }

    @Override
    public PaymentResult pay(final PaymentRequest request) {
        return register(request, TransactionType.PAYMENT);
    }

    @Override
    public PaymentResult refund(final PaymentRequest request) {
        return register(request, TransactionType.INFLOW);
    }

    private PaymentResult register(final PaymentRequest request, final TransactionType type) {
        try {
            var value = new Money(request.amount(), Currency.getInstance(request.currencyCode()));
            var command = new AddTransactionCommand(new CardNumber(request.cardNumber()), value, type);
            var transactionId = addTransactionUseCase.handle(command);
            return new PaymentResult.Completed(transactionId.value().toString());
        } catch (DomainException | CardNotFoundException | IllegalArgumentException exception) {
            return new PaymentResult.Rejected(exception.getMessage());
        }
    }

}
