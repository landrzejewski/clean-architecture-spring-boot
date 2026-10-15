# 6. Komunikacja między modułami: własny port + ACL, domyślnie asynchronicznie

- Status: zaakceptowana
- Branch: `14-module-communication`

## Kontekst
Zamówienia wywoływały `PaymentsApi` bezpośrednio z use case'u, więc rdzeń zamówień znał kontrakt płatności.
Synchroniczne wywołanie wiąże też dostępność i czas odpowiedzi obu modułów.

## Decyzja
- Zamówienia definiują własny port `Payments` i własny model wyniku (`PaymentOutcome`). Kontrakt płatności zna tylko adapter `orders.adapters.payments` (Anti-Corruption Layer: `PaymentsTranslator`).
- Wynik płatności wraca osobnym use case'em (`CompletePaymentUseCase`), więc adapter może działać synchronicznie lub asynchronicznie.
- Domyślnie komunikacja jest asynchroniczna: `PaymentRequested` → `PaymentProcessed`, korelacja po `reference` = id zamówienia.
- Zamówienie zapisujemy przed wysłaniem żądania płatności.

## Konsekwencje
- Spójność ostateczna: klient dostaje `202 Accepted` i status `AWAITING_PAYMENT`.
- Zdarzenia in-memory mogą zginąć (awaria między commitem a publikacją). Potrzebny outbox, patrz `15`.
- Testy E2E muszą czekać na wynik (Awaitility).
