# 20 – Saga: orkiestracja

> Moduł 4: Wzorce zaawansowane · bazuje na: `15-spring-modulith` · następny: `21-saga-choreography`

## Cel
Zapewnić spójność procesu biznesowego, który przechodzi przez kilka modułów z osobnymi transakcjami,
**bez transakcji rozproszonej**. Każdy krok ma krok kompensujący, a jeden orkiestrator prowadzi proces i pamięta jego stan.

## Problem z poprzedniego kroku
W `15` zamówienie kończyło się na płatności. Dochodzi wysyłka. Co zrobić, gdy karta została obciążona,
a wysyłka jest niemożliwa? Rollbacku już nie ma, bo transakcja płatności dawno się zatwierdziła.

## Przebieg
```
                        ┌──────────── OrderSagaOrchestrator (orders) ─────────────┐
POST /api/orders ──► start: CHARGE_PAYMENT ──► PaymentRequested ──► payments
                                        ◄── PaymentProcessed(Completed)
                     SCHEDULE_SHIPMENT ──► ShipmentRequested ──► shipping
                                        ◄── ShipmentProcessed(scheduled)    ──► CONFIRMED / COMPLETED
                                        ◄── ShipmentProcessed(rejected)
                     kompensacja (odwrotna kolejność): REFUND_PAYMENT ──► RefundRequested ──► payments
                                        ◄── RefundProcessed                 ──► CANCELLED / COMPENSATED
```
Adres zawierający `Antarctica` lub `Mars` jest poza obszarem dostaw, więc służy do demo kompensacji.

## Co się zmieniło
```bash
git diff 15-spring-modulith..20-saga-orchestration --stat
```
| Element | Gdzie |
|---|---|
| Moduł `shipping` (domena `Shipment`, `Address`, idempotentny `ScheduleShipmentService`) | `shipping.*` |
| Stan sagi (maszyna stanów, stos kompensacji, timeouty) | `orders.domain.OrderSaga`, `SagaStep`, `SagaStatus` |
| Orkiestrator | `orders.application.OrderSagaOrchestrator` |
| Porty uczestników | `orders.application.Payments` (+ `requestRefund`), `Shipping` |
| Trwałość sagi | `orders.adapters.persistence.JpaOrderSagaRepository` (`order_sagas`) |
| Ponawianie kroków | `orders.adapters.scheduling.SagaTimeoutScheduler` + `orders.saga.*` w `application.yml` |
| Zwrot środków (kompensacja) | `payments.api.RefundRequested/RefundProcessed`, `PaymentsApi.refund` |
| Idempotentność płatności | `payments.adapters.api.ProcessedMessages` (`processed_messages`) |
| Podgląd procesu | `GET /api/orders/{id}/saga` |

Usunięte: `CompletePaymentUseCase` i tryb synchroniczny (`SyncPaymentsAdapter`). Wynik każdego kroku trafia teraz do orkiestratora.

## Na co zwrócić uwagę
- **Kompensacja to nie rollback**: po anulowaniu karta ma 3 transakcje (wpłata, płatność, zwrot) i saldo jak na początku (`SagaEndToEndTest`).
- **Odwrotna kolejność**: `OrderSaga` trzyma ukończone kroki na stosie. Dodanie kroku (np. rezerwacja magazynu) to dopisanie go do `SagaStep` z jego kompensacją.
- **Idempotentność**: `PaymentsModuleTest.duplicated_payment_request_charges_card_only_once` sprawdza, że to samo żądanie dostarczone dwa razy obciąża kartę raz. Orkiestrator ignoruje odpowiedzi, na które nie czeka (`OrderSagaOrchestratorTest.duplicate_results_are_ignored`).
- **Recovery**: stan sagi jest w bazie, a polecenia w outboxie. Po restarcie `republish-outstanding-events-on-restart` dosyła niedostarczone komunikaty, a `SagaTimeoutScheduler` ponawia kroki bez odpowiedzi.
- **Timeout**: `OrderSagaOrchestratorTest.step_without_response_is_retried_and_then_compensated` pokazuje próby, a potem kompensację.
- Stan i polecenie zapisujemy w jednej transakcji. Gdyby było inaczej, mogłoby powstać polecenie bez stanu albo stan bez polecenia.

## Kiedy NIE stosować
Gdy wszystkie kroki są w jednym module i jednej bazie, wystarczy zwykła transakcja.
Gdy proces ma 2 kroki i rzadko zawodzi, prostsza bywa choreografia (`21`).

## Pytania do dyskusji
- Kompensacja w innej walucie i po innym kursie: czy zwrot zawsze jest „równy” płatności? Co z prowizją?
- Co zrobić z sagą w stanie `FAILED` (kompensacja się nie udała)?
- Czy klient powinien widzieć stany pośrednie (`PAID`)? Jak je nazwać w UI?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run      # client.http: zamówienie z adresem "Antarctica" -> kompensacja
```
