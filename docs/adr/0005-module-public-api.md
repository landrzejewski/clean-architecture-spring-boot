# 5. Moduły komunikują się wyłącznie przez publiczne API

- Status: zaakceptowana
- Branch: `11-language-encapsulation`

## Kontekst
W `10` zamówienia sięgały do `CardRepository` i agregatu `Card`, a płatności do zamówień. Powstał cykl
i ominięcie reguł płatności (waluty, prowizja, zdarzenia).

## Decyzja
- Każdy kontekst wystawia pakiet `api` z interfejsem i rekordami kontraktu (`payments.api.PaymentsApi`, `PaymentRequest`, `PaymentResult`).
- Implementacja API to package-private adapter (`PaymentsApiAdapter`), który woła use case.
- Wynik biznesowy (odrzucenie płatności) to zamknięta hierarchia `sealed`, a nie wyjątek przekraczający granicę modułu.
- Zależności między kontekstami muszą być acykliczne. Pilnuje tego `ContextBoundariesTest`.

## Konsekwencje
- Kontrakt jest mały i stabilny, a wnętrze płatności może się zmieniać.
- Java (bez JPMS) nie ukryje publicznych klas z podpakietów, więc granicę egzekwujemy testami ArchUnit. Silniejsze mechanizmy to moduły Maven (`12`) i JPMS (`13`).
