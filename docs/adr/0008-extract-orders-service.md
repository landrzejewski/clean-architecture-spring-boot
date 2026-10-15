# 8. Wydzielenie zamówień do osobnej usługi

- Status: zaakceptowana
- Branch: `16-extract-microservice`

## Kontekst
Zamówienia mają inny profil obciążenia i inny cykl wydawniczy niż płatności. Granica jest już sprawdzona
w modularnym monolicie (`15`): zamówienia znają tylko `payments::api` i komunikują się przez port `Payments`.

## Decyzja
- Dwie aplikacje: `payments-service` (8081) i `orders-service` (8082), każda z własną bazą i własnym outboxem (Event Publication Registry).
- Wspólne artefakty: `payments-api` (kontrakt: model HTTP, komunikaty, nazwy topiców, przykładowe wiadomości kontraktu) i `common` (biblioteka techniczna).
- Asynchronicznie: Kafka. Zdarzenia wychodzą przez eksternalizację Spring Modulith (outbox → Kafka), a przychodzące komunikaty zamieniamy na zdarzenia wewnętrzne (inbox).
- Synchronicznie (`orders.payments.mode=sync`): HTTP z `@Retryable` i `@ConcurrencyLimit` (Spring Framework 7).
- Kontrakt HTTP sprawdzają testy obu stron na wspólnych plikach `contracts/*.json`.

## Konsekwencje
- W zamówieniach zmieniły się wyłącznie adaptery i konfiguracja. Domena i use case'y bez zmian.
- Kontrakt sieciowy musiał się uprościć (`PaymentProcessed` bez zagnieżdżonej hierarchii sealed).
- Nowe problemy: dostępność drugiej usługi, at-least-once (idempotentność), obserwowalność, wersjonowanie kontraktu, infrastruktura (Kafka, dwie bazy).
