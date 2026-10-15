package pl.fullstackdeveloper.payments.application;

/*
 Zdarzenia aplikacyjne modułu płatności. Od 18 są źródłem danych dla modelu odczytu (projekcji),
 więc muszą nieść komplet informacji potrzebnych do jego zbudowania.
 */
public sealed interface PaymentsEvent permits CardAdded, TransactionAdded {

    String cardNumber();

}
