package pl.fullstackdeveloper.compare.onion.domain.services;

import pl.fullstackdeveloper.compare.onion.domain.model.Card;

// W onion interfejsy repozytoriów należą do pierścienia usług domenowych
public interface CardRepository {

    Card findByNumber(String number);

    void save(Card card);

}
