package pl.fullstackdeveloper.compare.hexagonal.application.port.in;

import java.math.BigDecimal;

public record TopUpCommand(String cardNumber, BigDecimal amount) {
}
