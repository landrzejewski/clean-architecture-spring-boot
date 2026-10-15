package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.common.cqrs.CommandHandler;
import pl.fullstackdeveloper.payments.domain.CardNumber;

// Port wejściowy (driving port) - kontrakt, który aplikacja oferuje światu zewnętrznemu
// Port wejściowy strony zapisu = handler komendy
public interface AddCardUseCase extends CommandHandler<AddCardCommand, CardNumber> {
}
