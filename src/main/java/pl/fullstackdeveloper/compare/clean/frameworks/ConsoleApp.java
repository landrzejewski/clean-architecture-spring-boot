package pl.fullstackdeveloper.compare.clean.frameworks;

import pl.fullstackdeveloper.compare.clean.entities.Card;
import pl.fullstackdeveloper.compare.clean.interfaceadapters.controllers.TopUpController;
import pl.fullstackdeveloper.compare.clean.interfaceadapters.gateways.InMemoryCardGateway;
import pl.fullstackdeveloper.compare.clean.interfaceadapters.presenters.TopUpPresenter;
import pl.fullstackdeveloper.compare.clean.usecases.TopUpInteractor;

import java.math.BigDecimal;

// Frameworks & Drivers - najbardziej zewnętrzny pierścień, tu składamy całość
public class ConsoleApp {

    public static void main(String[] args) {
        var gateway = new InMemoryCardGateway();
        gateway.save(new Card("1111222233334444", BigDecimal.ZERO));
        var presenter = new TopUpPresenter();
        var controller = new TopUpController(new TopUpInteractor(gateway, presenter));
        controller.topUp("1111222233334444", "100");
        System.out.println(presenter.getViewModel().message());
    }

}
