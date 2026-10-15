package pl.fullstackdeveloper.payments;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.fullstackdeveloper.payments.application.AddCardCommand;
import pl.fullstackdeveloper.payments.application.AddCardUseCase;
import pl.fullstackdeveloper.payments.application.GetCardUseCase;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

// Test "okablowania": Spring złożył te same klasy, które w 02 składaliśmy ręcznie
@SpringBootTest
@ActiveProfiles("demo")
class PaymentsContextTest {

    @Autowired
    private AddCardUseCase addCard;
    @Autowired
    private GetCardUseCase getCard;

    @Test
    void use_cases_are_wired_by_spring() {
        var cardNumber = addCard.handle(new AddCardCommand(Currency.getInstance("EUR")));

        assertThat(getCard.handle(cardNumber).balance().currency()).isEqualTo(Currency.getInstance("EUR"));
    }

}
