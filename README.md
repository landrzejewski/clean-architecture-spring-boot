# 16 – Wydzielenie mikroserwisu

> Moduł 3: Modularyzacja · poprzedni: `15-spring-modulith` · koniec modułu 3
> (moduł 4 startuje z `15`, nie z tego brancha)

## Cel
Pokazać migrację modularnego monolitu do usług jako **wymianę adapterów**. Moduł `orders` staje się
osobną aplikacją, a jego domena i use case'y się nie zmieniają. Zmienia się sposób komunikacji (sieć zamiast wywołań w procesie)
i pojawiają się nowe problemy: zawodna sieć, kontrakty, infrastruktura.

## Struktura
```
shop (pom)
├── common             biblioteka techniczna (adnotacje, aspekty, ...)
├── payments-api       kontrakt: PaymentsApi, PaymentRequest/Response (HTTP), PaymentRequested/Processed (Kafka),
│                      PaymentsTopics, contracts/*.json
├── payments-service   :8081  domena + use case'y płatności, REST, Kafka in/out, outbox
└── orders-service     :8082  domena + use case'y zamówień, REST, Kafka in/out, HTTP do płatności, outbox
```

## Co się zmieniło
```bash
git diff 15-spring-modulith..16-extract-microservice --stat -M
# domena i use case'y zamówień - tylko przeniesienie plików, zero zmian w treści:
git diff -M 15-spring-modulith..16-extract-microservice -- '*orders/domain/*' '*orders/application/*'
```
| Było w monolicie (`15`) | Jest w usługach (`16`) |
|---|---|
| `SyncPaymentsAdapter` → `PaymentsApi` w procesie | `HttpPaymentsAdapter` → `POST /api/payments` + `@Retryable` + `@ConcurrencyLimit` |
| `PaymentRequested` przez zdarzenia Springa | ten sam `AsyncPaymentsAdapter` + eksternalizacja do Kafki (`PaymentsMessagingConfiguration`) |
| `PaymentProcessedListener` | bez zmian, a komunikat z Kafki zamienia na zdarzenie `KafkaPaymentResultsListener` (inbox) |
| `PaymentRequestedListener` (płatności) | bez zmian, a wejście z Kafki obsługuje `KafkaPaymentRequestsListener` |
| `PaymentProcessed(reference, PaymentResult)` | płaski kontrakt JSON `PaymentProcessed(reference, status, paymentId, reason)` |
| jedna baza | dwie bazy, każda z własnym `event_publication` |

## Na co zwrócić uwagę
- **Domena i use case'y zamówień są nietknięte.** To efekt pracy z modułów 2–3: port `Payments`, ACL, `CompletePaymentUseCase`.
- **Kontrakt**:
  - `payments-api/src/main/resources/contracts/*.json` to przykładowe wiadomości,
  - `PaymentsContractProviderTest` (płatności) i `PaymentsContractConsumerTest` (zamówienia) sprawdzają je z obu stron.
- **Outbox + Kafka**: zdarzenie jest zapisywane w `event_publication` w transakcji biznesowej, a dopiero potem wysyłane do Kafki. Klucz komunikatu = `reference`, więc kolejność jest zachowana per zamówienie.
- **Inbox**: komunikat z Kafki staje się zdarzeniem wewnętrznym w transakcji, więc przetwarzanie korzysta z ponowień Event Publication Registry.
- **Sieć zawodzi**: wyłącz `payments-service` i złóż zamówienie w trybie `sync`. Zobaczysz kolejne próby (`@Retryable`), a potem błąd. Jaki kod HTTP powinien dostać klient? Może lepszy byłby tryb async?
- W trybie `sync` wywołanie HTTP odbywa się wewnątrz transakcji bazy zamówień (`@Atomic` na `PlaceOrderService`), więc długie trzymanie połączenia z bazą to typowy antywzorzec. Tryb async go nie ma.
- Celowo pominięte: service discovery, config server, API gateway, tracing. Patrz pytania.

## Kiedy NIE stosować
Gdy jedynym powodem jest „mikroserwisy są nowoczesne”. Modularny monolit (`15`) daje większość korzyści
(granice, niezależny rozwój modułów, testy) bez kosztów sieci, wdrożeń i spójności rozproszonej.

## Pytania do dyskusji
- Kto jest właścicielem `payments-api` i jak wersjonować kontrakt (np. dodanie pola, zmiana znaczenia)?
- Co się stanie, gdy `PaymentRequested` zostanie dostarczone dwa razy? Gdzie dodać deduplikację?
- Które z pominiętych elementów (discovery, gateway, tracing, circuit breaker) są potrzebne od pierwszego dnia?
- Jak przeprowadzić taką migrację na produkcji (strangler fig, dual-write, feature flagi)?

## Uruchomienie
```bash
./mvnw test                                   # testy Kafki (Testcontainers) wymagają Dockera
docker compose up -d                          # Kafka (+ PostgreSQL)
./mvnw -DskipTests package
java -jar payments-service/target/payments-service-1.0-SNAPSHOT.jar
java -jar orders-service/target/orders-service-1.0-SNAPSHOT.jar                         # async (Kafka)
java -jar orders-service/target/orders-service-1.0-SNAPSHOT.jar --orders.payments.mode=sync   # HTTP
```

---

## Ćwiczenie „blog”, część 3 (koniec modułu 3)
1. Dodaj do bloga drugi kontekst, **notifications**: po opublikowaniu artykułu i po dodaniu komentarza wyślij powiadomienie (na razie log). Notifications nie mogą znać wnętrza bloga.
2. Zrób z obu kontekstów moduły Spring Modulith:
   - zdarzenia `ArticlePublished` i `CommentAdded` jako API modułu bloga,
   - `@ApplicationModuleListener` w notifications,
   - `ApplicationModules.verify()` w testach.
3. Napisz `@ApplicationModuleTest` dla notifications z użyciem `Scenario` (`scenario.publish(...)`).
4. Dla chętnych: wydziel notifications do osobnej aplikacji z Kafką (eksternalizacja zdarzeń).
