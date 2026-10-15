package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.common.cqrs.CommandHandler;
import pl.fullstackdeveloper.payments.domain.TransactionId;

public interface AddTransactionUseCase extends CommandHandler<AddTransactionCommand, TransactionId> {
}
