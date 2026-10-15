package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.payments.domain.Card;
import pl.fullstackdeveloper.payments.domain.CardId;
import pl.fullstackdeveloper.payments.domain.CardNumber;

@Atomic
public class AddCardService implements AddCardUseCase {

    private static final int CARD_VALIDITY_IN_YEARS = 1;

    private final CardNumberGenerator cardNumberGenerator;
    private final DateTimeProvider dateTimeProvider;
    private final CardRepository cardRepository;

    public AddCardService(final CardNumberGenerator cardNumberGenerator, final DateTimeProvider dateTimeProvider,
                          final CardRepository cardRepository) {
        this.cardNumberGenerator = cardNumberGenerator;
        this.dateTimeProvider = dateTimeProvider;
        this.cardRepository = cardRepository;
    }

    @Override
    public CardNumber handle(final AddCardCommand command) {
        var expiration = dateTimeProvider.getZonedDateTime().toLocalDate().plusYears(CARD_VALIDITY_IN_YEARS);
        var card = new Card(new CardId(), cardNumberGenerator.getNext(), expiration, command.currency());
        return cardRepository.save(card).getNumber();
    }

}
