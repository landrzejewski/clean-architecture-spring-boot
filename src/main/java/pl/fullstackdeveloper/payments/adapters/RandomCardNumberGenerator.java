package pl.fullstackdeveloper.payments.adapters;

import org.springframework.beans.factory.annotation.Value;
import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.CardNumberGenerator;
import pl.fullstackdeveloper.payments.domain.CardNumber;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Adapter
final class RandomCardNumberGenerator implements CardNumberGenerator {

    private final SecureRandom random = new SecureRandom();
    private final int length;

    RandomCardNumberGenerator(@Value("${payments.card-number-length:16}") final int length) {
        this.length = length;
    }

    @Override
    public CardNumber getNext() {
        var number = random.ints(length, 0, 10)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining());
        return new CardNumber(number);
    }

}
