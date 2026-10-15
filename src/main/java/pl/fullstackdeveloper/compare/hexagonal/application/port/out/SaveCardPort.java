package pl.fullstackdeveloper.compare.hexagonal.application.port.out;

import pl.fullstackdeveloper.compare.hexagonal.domain.Card;

public interface SaveCardPort {

    void save(Card card);

}
