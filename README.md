# 14 – Komunikacja między modułami

> Moduł 3: Modularyzacja · poprzedni: `13-jpms` · następny: `15-spring-modulith`

## Cel
Pokazać dwa style integracji kontekstów, **synchroniczny** (wywołanie API) i **asynchroniczny** (zdarzenia),
schowane za tym samym portem wyjściowym zamówień. Do tego Anti-Corruption Layer, który tłumaczy język płatności na język zamówień.

## Problem z poprzedniego kroku
`PlaceOrderService` wołał `PaymentsApi` bezpośrednio, więc rdzeń zamówień zależał od kontraktu płatności.
Wywołanie synchroniczne oznacza też, że awaria lub spowolnienie płatności blokuje składanie zamówień.

## Co się zmieniło
```bash
git diff 13-jpms..14-module-communication --stat
```
| Element | Gdzie | Rola |
|---|---|---|
| `Payments` (port) | `orders.application` | „poproś o płatność”, w języku zamówień |
| `PaymentOutcome` (`Paid` / `Declined`) | `orders.application` | wynik w języku zamówień |
| `CompletePaymentUseCase` | `orders.application` | przyjęcie wyniku płatności |
| `SyncPaymentsAdapter` | `orders.adapters.payments` | `PaymentsApi.pay()` i od razu `CompletePayment` (`orders.payments.mode=sync`) |
| `AsyncPaymentsAdapter` + `PaymentProcessedListener` | `orders.adapters.payments` | publikuje `PaymentRequested`, odbiera `PaymentProcessed` (domyślnie) |
| `PaymentsTranslator` | `orders.adapters.payments` | ACL: `PaymentResult` → `PaymentOutcome` |
| `PaymentRequested`, `PaymentProcessed` | `payments-api` | kontrakt komunikatów |
| `PaymentRequestedListener` | `payments.adapters.api` | `@Async` przetwarzanie żądań płatności |

## Przepływ asynchroniczny
```
POST /api/orders ─► PlaceOrder ─► save(AWAITING_PAYMENT) ─► Payments.requestPayment
                                                             └─► event PaymentRequested(reference=orderId)
   202 Accepted ◄────────────────────────────────────────────────┘
                                   [wątek async] PaymentRequestedListener ─► PaymentsApi.pay ─► commit
                                                 └─► event PaymentProcessed(reference, result)
                                   PaymentProcessedListener ─► CompletePayment ─► PAID / REJECTED
```

## Na co zwrócić uwagę
- `PlaceOrderService` i `CompletePaymentService` nie zmieniają się przy przełączeniu `orders.payments.mode=sync|async`. Pokazują to `ShopEndToEndTest` i `ShopSynchronousPaymentsTest`.
- `OrdersArchitectureTest`: rdzeń zamówień nie zna `pl.fullstackdeveloper.payments..`, a `payments.api` zna tylko adapter.
- Korelacja przez `reference`: płatności nie wiedzą, czym jest zamówienie, tylko odsyłają identyfikator.
- **Słaby punkt**: zdarzenia są w pamięci. Restart lub awaria między commitem płatności a publikacją `PaymentProcessed` zostawia zamówienie w `AWAITING_PAYMENT` na zawsze. Rozwiązanie w `15`.

## Kiedy NIE stosować
Asynchroniczność dodaje stany pośrednie, retry i trudniejsze debugowanie. Gdy operacja musi dać natychmiastową,
spójną odpowiedź (np. autoryzacja płatności przy kasie), synchroniczne wywołanie bywa właściwym wyborem.

## Pytania do dyskusji
- Kto jest właścicielem `PaymentRequested`: nadawca czy odbiorca? Dlaczego leży w `payments-api`?
- Co powinien zobaczyć klient po `202 Accepted`? Polling, webhook, SSE?
- Jak obsłużyć zdublowane `PaymentProcessed` (idempotentność)?

## Uruchomienie
```bash
./mvnw test
./mvnw -DskipTests package && java -jar app/target/app-1.0-SNAPSHOT.jar --orders.payments.mode=async
```
