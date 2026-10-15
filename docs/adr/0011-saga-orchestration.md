# 11. Saga (orkiestracja) dla procesu składania zamówienia

- Status: zaakceptowana
- Branch: `20-saga-orchestration` (bazuje na `15`; ADR 0008–0010 należą do branchy 16, 18, 19)

## Kontekst
Złożenie zamówienia obejmuje trzy moduły z osobnymi transakcjami: płatności, wysyłki i zamówienia.
Rozproszona transakcja (2PC) nie wchodzi w grę, a spójność biznesowa musi być zachowana także przy błędach.

## Decyzja
- Proces prowadzi orkiestrator (`OrderSagaOrchestrator`) w module zamówień. Uczestnicy dostają polecenia i odpowiadają zdarzeniami.
- Stan sagi (`OrderSaga`: status, bieżący krok, stos ukończonych kroków, próby) jest trwały i zapisywany w tej samej transakcji co wysyłane polecenie (outbox).
- Porażka kroku uruchamia kompensacje ukończonych kroków w odwrotnej kolejności. Kompensacja jest semantyczna (zwrot środków), a nie rollback.
- Kroki są idempotentne (`ProcessedMessages` w płatnościach, unikalna referencja wysyłki), a orkiestrator ignoruje odpowiedzi, na które nie czeka.
- Kroki bez odpowiedzi są ponawiane po `orders.saga.step-timeout`, a po `max-attempts` uznawane za nieudane.

## Konsekwencje
- Proces jest widoczny w jednym miejscu (`GET /api/orders/{id}/saga`) i łatwy do zmiany.
- Orkiestrator zna wszystkich uczestników (centralizacja, ryzyko „god service”).
- Stany pośrednie są widoczne dla klientów (`AWAITING_PAYMENT`, `PAID`). Spójność jest ostateczna.
- Nieudana kompensacja kończy się stanem `FAILED`, który wymaga procesu operacyjnego (alert, ręczna obsługa).
