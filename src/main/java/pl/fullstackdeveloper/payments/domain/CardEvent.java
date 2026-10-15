package pl.fullstackdeveloper.payments.domain;

/*
 Zdarzenia domenowe karty. W Event Sourcingu są ŹRÓDŁEM PRAWDY - stan agregatu to wynik
 zastosowania (apply) wszystkich zdarzeń w kolejności. Zamknięta hierarchia: każdy nowy fakt
 wymaga jawnej obsługi w Card.apply().
 */
public sealed interface CardEvent permits CardIssued, TransactionRegistered {

    CardNumber cardNumber();

}
