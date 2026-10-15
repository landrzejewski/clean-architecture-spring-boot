# 10 – Drugi kontekst „na skróty”

> Moduł 3: Modularyzacja · poprzedni: `09-testing-architecture` · następny: `11-language-encapsulation`

## Cel
Dodać drugi bounded context, `orders` (zamówienia opłacane kartą), w sposób, w jaki zwykle dzieje się to pod presją czasu:
konteksty sięgają nawzajem do swoich wnętrz. To punkt wyjścia całego modułu o modularyzacji.

## Co się zmieniło
```bash
git diff 09-testing-architecture..10-second-context-naive --stat
```
- `orders.domain`: `Order`, `OrderId`, `OrderStatus`
- `orders.application`: `PlaceOrderService`, `GetOrderService`, `GetCardOrdersService` i port `OrderRepository`
- `orders.adapters`: `InMemoryOrderRepository`, `OrdersRestController` (`POST /api/orders`, `GET /api/orders/{id}`)
- `payments.adapters.rest.GetCardOrdersRestController` (`GET /api/cards/{number}/orders`)
- `ContextBoundariesTest`: reguły granic kontekstów, **celowo wyłączone**
- `docs/c4/workspace.dsl`: diagram komponentów z dwoma kontekstami i cyklem

## Problemy do znalezienia
1. **`orders` zna wnętrze `payments`.** `PlaceOrderService` używa `CardRepository`, `Card` i `Transaction` i sam obciąża kartę.
2. **Ominięty use case.** Brak przeliczania walut, prowizji, zdarzeń i `@Retry`. `NaiveOrdersIntegrationTest` pokazuje, że płatność w EUR działa, a zamówienie w EUR zostaje odrzucone.
3. **Cykl między kontekstami.** `payments` (REST) zależy od `orders`, a `orders` od `payments`. Nie da się ich rozdzielić ani zmieniać niezależnie.
4. **Wspólny model.** `Order` używa `payments.domain.Money` i `CardNumber`, więc każda zmiana w płatnościach może zepsuć zamówienia.
5. **Brak egzekucji.** Wszystko jest `public`, więc kompilator na wszystko pozwala.

Usuń `@Disabled` w `ContextBoundariesTest` i uruchom `./mvnw test -Dtest=ContextBoundariesTest`, żeby zobaczyć listę naruszeń.

## Pytania do dyskusji
- Kto jest właścicielem reguły „karta musi mieć środki”? Czy zamówienia powinny ją znać?
- Co jest publicznym kontraktem płatności? Czego zamówienia naprawdę potrzebują?
- Czy `Money` może być współdzielone między kontekstami (shared kernel)?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run
```
