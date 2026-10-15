# 18 – CQRS z osobnym magazynem odczytu

> Moduł 4: Wzorce zaawansowane · poprzedni: `17-cqrs-light` · następny: `19-event-sourcing`

## Cel
Przenieść model odczytu do **osobnej bazy** (MongoDB) i zasilać go **zdarzeniami** ze strony zapisu.
To klasyczny CQRS ze slajdów: Command → Command Handler → agregat → Event Bus → Event Handler → read database.

## Problem z poprzedniego kroku
Strona odczytu w `17` czytała tabele zapisu: wspólny schemat, wspólne obciążenie, brak niezależnego skalowania.
Każda zmiana tabel strony zapisu groziła zepsuciem zapytań.

## Przepływ
```
POST /transactions ─► AddTransactionCommand ─► AddTransactionService ─► Card (agregat) ─► baza zapisu (H2/PostgreSQL)
                                                        └─► TransactionAdded ─► event_publication (outbox) ─┐
                                                                                                           │ po commicie, async
GET /api/cards/{n} ─► GetCardSummaryQuery ─► MongoDB (card_summaries) ◄─ CardSummaryProjector ◄────────────┘
```

## Co się zmieniło
```bash
git diff 17-cqrs-light..18-cqrs-read-store --stat
```
| Element | Opis |
|---|---|
| `PaymentsEvent` (sealed: `CardAdded`, `TransactionAdded`) + port `PaymentsEventPublisher` | zdarzenia niosą komplet danych potrzebnych projekcji |
| `SpringPaymentsEventPublisher` | publikacja w transakcji use case'u (zamiast logowania na konsolę) |
| `queries.CardSummaryDocument` | dokument MongoDB: saldo policzone z góry, transakcje osadzone |
| `queries.CardSummaryProjector` | zdarzenia → dokument; idempotentny i odporny na kolejność |
| `queries.*Handler` | odczyty z MongoDB zamiast SQL |
| `CardProjectionRebuilder` + `POST /api/admin/projections/cards/rebuild` | odbudowa modelu odczytu ze źródła prawdy |
| `MongoReadStore` (test) | MongoDB w Testcontainers |

## Na co zwrócić uwagę
- **Spójność ostateczna**: wykonaj w `client.http` wpłatę i od razu odczyt karty. Czasem zobaczysz stary stan. Testy czekają (`await()`).
- **Idempotentność**: `updateFirst(... transactions.id != id ...)` sprawia, że ponowione zdarzenie (at-least-once) nie zdubluje transakcji ani nie podwoi salda.
- **Kolejność**: `TransactionAdded` może dotrzeć przed `CardAdded`. Oba handlery robią upsert.
- **Odbudowa**: skasuj kolekcję `card_summaries` i wywołaj endpoint rebuild. Model odczytu jest tylko pochodną.
- `ArchitectureTest.read_side_is_independent_of_write_model`: strona odczytu zna tylko zdarzenia (`PaymentsEvent`), nie domenę.
- Kontrakt REST i strona zapisu bez zmian (poza publikowaniem `CardAdded`).

## Kiedy NIE stosować
Gdy użytkownik musi natychmiast zobaczyć skutek swojej operacji (read-your-writes), a nie da się tego obejść w UI.
Gdy obciążenie odczytów nie uzasadnia drugiej bazy.

## Pytania do dyskusji
- Jak pokazać użytkownikowi świeże dane zaraz po zapisie (odpowiedź z komendy, wersja, polling)?
- Co, jeśli projekcja ma błąd i przez tydzień liczyła saldo źle?
- Czy zdarzenia „pod projekcje” (z kompletem danych) to dobre zdarzenia domenowe?

## Uruchomienie
```bash
./mvnw test                         # testy MongoDB wymagają Dockera
docker compose up -d mongo
./mvnw spring-boot:run
```
