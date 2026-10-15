package pl.fullstackdeveloper.compare.hexagonal.application.service;

import pl.fullstackdeveloper.compare.hexagonal.application.port.in.TopUpCommand;
import pl.fullstackdeveloper.compare.hexagonal.application.port.in.TopUpUseCase;
import pl.fullstackdeveloper.compare.hexagonal.application.port.out.LoadCardPort;
import pl.fullstackdeveloper.compare.hexagonal.application.port.out.SaveCardPort;

import java.math.BigDecimal;

public class TopUpService implements TopUpUseCase {

    private final LoadCardPort loadCardPort;
    private final SaveCardPort saveCardPort;

    public TopUpService(LoadCardPort loadCardPort, SaveCardPort saveCardPort) {
        this.loadCardPort = loadCardPort;
        this.saveCardPort = saveCardPort;
    }

    @Override
    public BigDecimal topUp(TopUpCommand command) {
        var card = loadCardPort.load(command.cardNumber());
        card.topUp(command.amount());
        saveCardPort.save(card);
        return card.getBalance();
    }

}
