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
                                         final CardRepository cardRepository, final PaymentsEventPublisher eventPublisher) {
        return new AddCardService(cardNumberGenerator, dateTimeProvider, cardRepository, eventPublisher);
    }

    @Bean
    public AddTransactionUseCase addTransactionUseCase(final DateTimeProvider dateTimeProvider,
                                                       final PaymentsEventPublisher eventPublisher,
                                                       final CardRepository cardRepository,
                                                       final ExchangeRateProvider exchangeRateProvider) {
        return new AddTransactionService(dateTimeProvider, eventPublisher, cardRepository, exchangeRateProvider);
    }

}
