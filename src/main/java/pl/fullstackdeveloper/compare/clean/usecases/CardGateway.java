package pl.fullstackdeveloper.compare.clean.usecases;

import pl.fullstackdeveloper.compare.clean.entities.Card;

public interface CardGateway {

    Card findByNumber(String number);

    void save(Card card);

}
