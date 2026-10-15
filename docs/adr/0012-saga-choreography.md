# 12. Saga w wariancie choreografii (porównanie z ADR 0011)

- Status: zaakceptowana na potrzeby porównania
- Branch: `21-saga-choreography`

## Kontekst
Orkiestrator z `20` centralizuje wiedzę o procesie. Alternatywa to choreografia: moduły reagują na fakty
(zdarzenia) innych modułów, a proces wynika z tych reakcji.

## Decyzja
- Zdarzenia procesu (`OrderPlaced`, `PaymentCompleted`, `PaymentFailed`, `ShipmentScheduled`, `ShipmentFailed`, `PaymentRefunded`) trafiają do wspólnego modułu `events` (published language). Gdyby leżały w modułach-nadawcach, zależności tworzyłyby cykl.
- Każdy uczestnik przechowuje lokalnie dane potrzebne do swoich reakcji (`OrderPayment`, `PendingShipment`).
- Reakcje są idempotentne. Nieudane publikacje (np. zdarzenie przed zdarzeniem, od którego zależy) są ponawiane automatycznie (`EventPublicationRetryScheduler`).

## Konsekwencje
- Brak centralnego punktu awarii i wiedzy. Moduły są luźniej powiązane w czasie wykonania.
- Proces nie istnieje w kodzie jako całość. Trzeba go zrekonstruować z listenerów i dokumentacji, a jego stan z wielu tabel.
- Trudniej wykryć „zawieszone” procesy i nieudane kompensacje, bo potrzebny jest monitoring zdarzeń.
