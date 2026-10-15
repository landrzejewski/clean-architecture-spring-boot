package pl.fullstackdeveloper.payments.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void money_cannot_be_negative() {
        assertThatThrownBy(() -> Money.of("-1", "PLN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void money_in_different_currencies_cannot_be_added() {
        assertThatThrownBy(() -> Money.of("1", "PLN").add(Money.of("1", "EUR")))
                .isInstanceOf(MismatchedCurrencyException.class);
    }

    @Test
    void money_is_a_value_object() {
        assertThat(Money.of("10", "PLN")).isEqualTo(Money.of("10", "PLN"));
    }

}
