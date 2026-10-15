package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.TransactionId;

public interface AddTransactionUseCase {

    TransactionId handle(AddTransactionCommand command);

}
