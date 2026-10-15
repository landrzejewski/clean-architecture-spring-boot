# 11 – Hermetyzacja na poziomie języka

> Moduł 3: Modularyzacja · poprzedni: `10-second-context-naive` · następny: `12-maven-modules`

## Cel
Naprawić problemy z `10` środkami samego języka Java i testów: wąski publiczny kontrakt modułu,
ukrycie implementacji (package-private), zamknięte hierarchie (`sealed`) i brak cykli.

## Co się zmieniło
```bash
git diff 10-second-context-naive..11-language-encapsulation --stat
```
| Problem z 10 | Rozwiązanie w 11 |
|---|---|
| `orders` używa `CardRepository` i `Card` | `payments.api.PaymentsApi` + `PaymentRequest` / `PaymentResult` (typy proste) |
| Ominięty use case płatności | `PaymentsApiAdapter` (package-private) woła `AddTransactionUseCase` |
| Wyjątki domenowe płatności w zamówieniach | `sealed interface PaymentResult { Completed, Rejected }` + `switch` z pattern matching |
| Cykl przez `GetCardOrdersRestController` | endpoint przeniesiony do `orders`: `GET /api/orders?cardNumber=…` |
| `Order` używa `Money`, `CardNumber` z payments | własne VO zamówień: `Price`, `CardReference` |
| Reguły wyłączone | `ContextBoundariesTest` włączony: brak cykli, `orders → payments.api` only |

Dodatkowo `DomainException` jest `sealed` z jawną listą dozwolonych wyjątków.

## Na co zwrócić uwagę
- `PlaceOrderService` używa `switch` po `PaymentResult` bez `default`. Kompilator wymusi obsługę nowego przypadku.
- `PlaceOrderService` nie ma `@Atomic`. Płatność commituje się we własnej transakcji modułu płatności. **Jedna transakcja na moduł** to fundament późniejszej komunikacji przez zdarzenia (`14`, `15`).
- **Ograniczenie Javy**: package-private działa tylko w obrębie jednego pakietu. `payments.domain` i `payments.application` muszą być `public`, bo korzystają z nich inne pakiety płatności. Kompilator nie zabroni `orders` ich użycia, więc granicę pilnuje tylko test ArchUnit.
- `OrdersIntegrationTest`: zamówienie w EUR jest teraz opłacane, bo działa logika walut z use case'u.

## Kiedy NIE stosować
Osobny kontrakt API w monolicie ma sens, gdy moduły rozwijają różne osoby lub zespoły albo planujemy wydzielenie.
Dla dwóch małych pakietów w jednym zespole może wystarczyć konwencja.

## Pytania do dyskusji
- `PaymentResult.Rejected` czy wyjątek? Kiedy odrzucenie jest „normalnym” wynikiem?
- Czy `CardReference` to dobra nazwa? Jak język zamówień różni się od języka płatności?
- Kto jest właścicielem kontraktu `payments.api`: dostawca czy konsument?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run
```
