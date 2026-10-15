package pl.fullstackdeveloper.compare.hexagonal.application.port.in;

import java.math.BigDecimal;

// Port wejściowy (driving / primary)
public interface TopUpUseCase {

    BigDecimal topUp(TopUpCommand command);

}
