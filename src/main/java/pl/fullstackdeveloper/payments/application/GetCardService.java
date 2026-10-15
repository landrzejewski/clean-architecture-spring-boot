package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.payments.domain.CardNumber;

public class GetCardService implements GetCardUseCase {

    private final CardRepository cardRepository;

    public GetCardService(final CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public CardView handle(final CardNumber cardNumber) {
        return cardRepository.findByNumber(cardNumber)
                .map(CardView::from)
                .orElseThrow(CardNotFoundException::new);
    }

}
