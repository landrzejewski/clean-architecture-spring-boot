package pl.fullstackdeveloper.payments.application;

import pl.fullstackdeveloper.common.annotations.Atomic;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.ResultPage;

@Atomic(readOnly = true)
public class GetCardsService implements GetCardsUseCase {

    private final CardRepository cardRepository;

    public GetCardsService(final CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public ResultPage<CardView> handle(final PageSpec pageSpec) {
        return cardRepository.findAll(pageSpec).map(CardView::from);
    }

}
