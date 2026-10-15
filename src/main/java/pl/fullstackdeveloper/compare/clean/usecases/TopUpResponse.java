package pl.fullstackdeveloper.compare.clean.usecases;

import java.math.BigDecimal;

public record TopUpResponse(String cardNumber, BigDecimal balance) {
}
