package pl.fullstackdeveloper.payments.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.ExchangeRate;
import pl.fullstackdeveloper.payments.domain.InsufficientBalanceException;
import pl.fullstackdeveloper.payments.domain.Money;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

class AddTransactionServiceTest {

    private static final ZonedDateTime NOW = ZonedDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneId.of("Europe/Warsaw"));
    private static final CardNumber CARD_NUMBER = new CardNumber("1111222233334444");

    private final CardRepository cardRepository = new InMemoryCardRepository();
    private final DateTimeProvider fixedClock = () -> NOW;
    private final List<TransactionAdded> publishedEvents = new ArrayList<>();
    private final ExchangeRateProvider exchangeRates = (from, to) -> new ExchangeRate(from, to, new BigDecimal("4"));
    private final AddTransactionUseCase addTransaction =
            new AddTransactionService(fixedClock, publishedEvents::add, cardRepository, exchangeRates);

    @BeforeEach
    void beforeEach() {
        var addCard = new AddCardService(() -> CARD_NUMBER, fixedClock, cardRepository);
        addCard.handle(new AddCardCommand(Currency.getInstance("PLN")));
    }

    @Test
    void transaction_is_registered_and_event_is_published() {
        var transactionId = addTransaction.handle(new AddTransactionCommand(CARD_NUMBER, Money.of("100", "PLN"), INFLOW));

        assertThat(cardRepository.findByNumber(CARD_NUMBER).orElseThrow().getBalance()).isEqualTo(Money.of("100", "PLN"));
        assertThat(publishedEvents)
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.transactionId()).isEqualTo(transactionId.value().toString());
                    assertThat(event.timestamp()).isEqualTo(NOW);
                });
    }

    @Test
    void no_event_is_published_when_domain_rejects_transaction() {
        assertThatThrownBy(() -> addTransaction.handle(new AddTransactionCommand(CARD_NUMBER, Money.of("100", "PLN"), PAYMENT)))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(publishedEvents).isEmpty();
    }

    @Test
    void foreign_payment_is_converted_to_card_currency_with_fee() {
        addTransaction.handle(new AddTransactionCommand(CARD_NUMBER, Money.of("1000", "PLN"), INFLOW));

        addTransaction.handle(new AddTransactionCommand(CARD_NUMBER, Money.of("100", "EUR"), PAYMENT));

        // 100 EUR * 4 = 400 PLN + 2% = 408 PLN
        assertThat(cardRepository.findByNumber(CARD_NUMBER).orElseThrow().getBalance()).isEqualTo(Money.of("592", "PLN"));
    }

    @Test
    void unknown_card_is_reported() {
        var unknownCard = new CardNumber("9999888877776666");

        assertThatThrownBy(() -> addTransaction.handle(new AddTransactionCommand(unknownCard, Money.of("1", "PLN"), INFLOW)))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void invalid_command_cannot_be_created() {
        assertThatThrownBy(() -> new AddTransactionCommand(CARD_NUMBER, Money.of("0", "PLN"), INFLOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
