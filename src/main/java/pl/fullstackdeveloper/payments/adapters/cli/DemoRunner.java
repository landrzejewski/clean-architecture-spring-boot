package pl.fullstackdeveloper.payments.adapters.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import pl.fullstackdeveloper.common.PageSpec;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.common.cqrs.Bus;
import pl.fullstackdeveloper.payments.application.*;
import pl.fullstackdeveloper.payments.domain.DomainException;
import pl.fullstackdeveloper.payments.domain.Money;
import pl.fullstackdeveloper.payments.queries.GetCardsQuery;

import java.util.Currency;

import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

// Adapter wejściowy "konsolowy" - zastępuje main() z 02/04; REST pojawi się w 06
@Adapter
@Profile("demo")
class DemoRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRunner.class);

    private final AddCardUseCase addCard;
    private final AddTransactionUseCase addTransaction;
    private final Bus bus;

    DemoRunner(final AddCardUseCase addCard, final AddTransactionUseCase addTransaction, final Bus bus) {
        this.addCard = addCard;
        this.addTransaction = addTransaction;
        this.bus = bus;
    }

    @Override
    public void run(final ApplicationArguments args) {
        var cardNumber = addCard.handle(new AddCardCommand(Currency.getInstance("PLN")));
        addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of("1000", "PLN"), INFLOW));
        try {
            addTransaction.handle(new AddTransactionCommand(cardNumber, Money.of("5000", "PLN"), PAYMENT));
        } catch (DomainException exception) {
            LOGGER.info("Rejected: {}", exception.getMessage());
        }
        LOGGER.info("{}", bus.ask(new GetCardsQuery(new PageSpec(0, 10))));
    }

}
