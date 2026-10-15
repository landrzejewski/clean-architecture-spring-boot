package pl.fullstackdeveloper.compare.clean.usecases;

import java.math.BigDecimal;

public record TopUpRequest(String cardNumber, BigDecimal amount) {
}
