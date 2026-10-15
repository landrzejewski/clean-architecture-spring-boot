package pl.fullstackdeveloper.compare.hexagonal.application.port.out;

import pl.fullstackdeveloper.compare.hexagonal.domain.Card;

// Porty wyjściowe (driven / secondary) - wąskie, skrojone pod potrzeby use case'u (ISP)
public interface LoadCardPort {

    Card load(String cardNumber);

}
