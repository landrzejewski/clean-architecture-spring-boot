package pl.fullstackdeveloper.payments;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.fullstackdeveloper.payments.application.*;

/*
 Composition root modułu płatności. Use case'y to zwykłe klasy bez adnotacji Springa -
 rejestrujemy je jako beany tutaj, dzięki czemu warstwa aplikacji nie zależy od frameworka.
 Adaptery (@Adapter) Spring znajduje sam przez component scan.
 */
@Configuration
public class PaymentsConfiguration {

    @Bean
    public AddCardUseCase addCardUseCase(final CardNumberGenerator cardNumberGenerator, final DateTimeProvider dateTimeProvider,
                                         final CardRepository cardRepository) {
        return new AddCardService(cardNumberGenerator, dateTimeProvider, cardRepository);
    }

    @Bean
    public AddTransactionUseCase addTransactionUseCase(final DateTimeProvider dateTimeProvider,
                                                       final TransactionEventPublisher transactionEventPublisher,
                                                       final CardRepository cardRepository,
                                                       final ExchangeRateProvider exchangeRateProvider) {
        return new AddTransactionService(dateTimeProvider, transactionEventPublisher, cardRepository, exchangeRateProvider);
    }

    @Bean
    public GetCardUseCase getCardUseCase(final CardRepository cardRepository) {
        return new GetCardService(cardRepository);
    }

    @Bean
    public GetCardsUseCase getCardsUseCase(final CardRepository cardRepository) {
        return new GetCardsService(cardRepository);
    }

}
