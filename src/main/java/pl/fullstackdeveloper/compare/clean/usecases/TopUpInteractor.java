package pl.fullstackdeveloper.compare.clean.usecases;

// Interactor - reguły biznesowe aplikacji (application business rules)
public class TopUpInteractor implements TopUpInputBoundary {

    private final CardGateway cardGateway;
    private final TopUpOutputBoundary presenter;

    public TopUpInteractor(CardGateway cardGateway, TopUpOutputBoundary presenter) {
        this.cardGateway = cardGateway;
        this.presenter = presenter;
    }

    @Override
    public void topUp(TopUpRequest request) {
        var card = cardGateway.findByNumber(request.cardNumber());
        card.topUp(request.amount());
        cardGateway.save(card);
        presenter.present(new TopUpResponse(card.getNumber(), card.getBalance()));
    }

}
