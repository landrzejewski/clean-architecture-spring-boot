package pl.fullstackdeveloper.compare.clean.interfaceadapters.presenters;

import pl.fullstackdeveloper.compare.clean.usecases.TopUpOutputBoundary;
import pl.fullstackdeveloper.compare.clean.usecases.TopUpResponse;

public class TopUpPresenter implements TopUpOutputBoundary {

    private TopUpViewModel viewModel;

    @Override
    public void present(TopUpResponse response) {
        viewModel = new TopUpViewModel("Balance: " + response.balance());
    }

    public TopUpViewModel getViewModel() {
        return viewModel;
    }

}
