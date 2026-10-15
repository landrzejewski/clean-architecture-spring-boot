package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.CardNumber;

// Port wejściowy (driving port) - kontrakt, który aplikacja oferuje światu zewnętrznemu
public interface AddCardUseCase {

    CardNumber handle(AddCardCommand command);

}
