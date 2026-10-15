package pl.fullstackdeveloper.payments.api;

/*
 Zamknięta hierarchia wyników (sealed) - klient musi obsłużyć każdy przypadek,
 a kompilator sprawdza kompletność switch-a. Odrzucenie płatności to zwykły wynik, nie wyjątek.
 */
public sealed interface PaymentResult {

    record Completed(String paymentId) implements PaymentResult {
    }

    record Rejected(String reason) implements PaymentResult {
    }

}
