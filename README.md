# 15 – Spring Modulith

> Moduł 3: Modularyzacja · poprzedni: `14-module-communication` · następny: `16-extract-microservice`
> (moduł 4 – wzorce zaawansowane – startuje z tego brancha: `17-cqrs-light`, `20-saga-orchestration`)

## Cel
Uzyskać granice modułów z `11`–`14` taniej: jeden artefakt, moduły jako pakiety, weryfikacja przez Spring Modulith.
Do tego **niezawodne zdarzenia** (transactional outbox), testy modułów w izolacji i dokumentacja generowana z kodu.

## Problem z poprzedniego kroku
- Pięć modułów Maven, `module-info`, testowe aplikacje per moduł: dużo infrastruktury.
- Zdarzenia w pamięci. Awaria między commitem płatności a publikacją `PaymentProcessed` = zamówienie „wisi” w `AWAITING_PAYMENT`.

## Co się zmieniło
```bash
git diff 14-module-communication..15-spring-modulith --stat -M
```
| Element | Gdzie |
|---|---|
| Moduły aplikacyjne | `payments`, `orders`, `common` (`package-info.java` z `@ApplicationModule`) |
| API płatności | `payments.api` z `@NamedInterface("api")`; `orders` ma `allowedDependencies = {"payments::api", "common"}` |
| Weryfikacja granic | `ApplicationModularityTest.modules_respect_boundaries` (zastępuje `ContextBoundariesTest`, moduły Maven i JPMS) |
| Outbox | `spring-modulith-starter-jpa`, tabela `event_publication`, `republish-outstanding-events-on-restart` |
| Listenery | `@ApplicationModuleListener` (async + nowa transakcja + after commit) w `PaymentRequestedListener` i `PaymentProcessedListener` |
| Zamówienia w bazie | `orders.adapters.persistence.JpaOrderRepositoryAdapter`. Zapis zamówienia i `PaymentRequested` w jednej transakcji |
| Odrzucenie ≠ awaria | `@Atomic(noRollbackFor = …)` w `AddTransactionService` |
| Testy modułów | `OrdersModuleTest`, `PaymentsModuleTest` (`@ApplicationModuleTest` + `Scenario`) |
| Demo ponawiania | `EventPublicationRegistryTest`: listener pada, publikacja zostaje, `FailedEventPublications.resubmit()` |
| Dokumentacja z kodu | `ApplicationModularityTest.writes_documentation` → `target/spring-modulith-docs`, kopia w `docs/modulith` |

## Na co zwrócić uwagę
- Spróbuj w `orders` użyć `pl.fullstackdeveloper.payments.domain.Card` i uruchom `ApplicationModularityTest`:
  `Module 'orders' depends on module 'payments' via … Allowed targets: payments :: api, common.`
- `OrdersModuleTest` uruchamia **tylko** moduł zamówień. Płatności „udajemy” zdarzeniami (`scenario.publish(...)`, `andWaitForEventOfType(...)`).
- `PaymentRequestedListener`: obciążenie karty, publikacja `PaymentProcessed` i oznaczenie `PaymentRequested` jako przetworzonego dzieje się w jednej transakcji.
- Konsola H2 (`/h2`) pokazuje tabelę `EVENT_PUBLICATION` z datą publikacji i ukończenia każdego zdarzenia.
- Dostarczanie at-least-once oznacza, że przy ponowieniu listener może dostać to samo zdarzenie drugi raz. Jak zabezpieczyć `PaymentRequestedListener` przed podwójnym obciążeniem? (wątek wraca w `20`)

## Moduły Maven / JPMS / Modulith
| | Maven (`12`) | JPMS (`13`) | Modulith (`15`) |
|---|---|---|---|
| Kiedy działa | kompilacja | kompilacja (+ runtime na module path) | testy |
| Granulacja | artefakt | pakiet (`exports`) | pakiet (API + named interfaces) |
| Koszt | poms, wersjonowanie | `module-info`, refleksja | prawie zerowy |
| Bonus | osobne wydania | silna hermetyzacja | outbox, testy modułów, dokumentacja |

## Kiedy NIE stosować
Gdy moduły mają być wydawane i wersjonowane niezależnie albo rozwijane przez osobne zespoły w osobnych repozytoriach.
Wtedy moduły Maven (lub osobne usługi) dają twardsze granice.

## Pytania do dyskusji
- Czy test łapiący naruszenie granicy wystarczy, czy potrzebujesz kompilatora?
- Gdzie powinna żyć logika idempotentności: w listenerze, w use case czy w domenie?
- Co wybrać po wydzieleniu modułu do osobnej usługi: Event Publication Registry, czy broker z własnym outboxem?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run      # http://localhost:8080/h2 (JDBC URL: jdbc:h2:mem:payments) -> tabela EVENT_PUBLICATION
```
