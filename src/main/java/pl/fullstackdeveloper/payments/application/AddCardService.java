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
    private final PaymentsEventPublisher eventPublisher;

    public AddCardService(final CardNumberGenerator cardNumberGenerator, final DateTimeProvider dateTimeProvider,
                          final CardRepository cardRepository, final PaymentsEventPublisher eventPublisher) {
        this.cardNumberGenerator = cardNumberGenerator;
        this.dateTimeProvider = dateTimeProvider;
        this.cardRepository = cardRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CardNumber handle(final AddCardCommand command) {
        var expiration = dateTimeProvider.getZonedDateTime().toLocalDate().plusYears(CARD_VALIDITY_IN_YEARS);
        var card = new Card(new CardId(), cardNumberGenerator.getNext(), expiration, command.currency());
        cardRepository.save(card);
        card.pullEvents().stream().map(PaymentsEvents::from).forEach(eventPublisher::publish);
        return card.getNumber();
    }

}
