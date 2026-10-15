# 19 – Event Sourcing

> Moduł 4: Wzorce zaawansowane · poprzedni: `18-cqrs-read-store` · koniec ścieżki CQRS/ES

## Cel
Zamiast zapisywać **stan** karty, zapisujemy **zdarzenia**, które do niego doprowadziły. Stan agregatu
odtwarzamy z historii, a modele odczytu (MongoDB z `18`) możemy w każdej chwili zbudować od zera przez replay.

## Problem z poprzedniego kroku
- Tabele `cards`/`transactions` przechowywały stan, a zdarzenia były tylko „powiadomieniem”. Gdyby projekcja zgubiła zdarzenie, nie byłoby skąd go wziąć.
- Przebudowa modelu odczytu w `18` czytała tabele zapisu, a nie zdarzenia, więc nowy widok nie miał dostępu do pełnej historii.

## Co się zmieniło
```bash
git diff 18-cqrs-read-store..19-event-sourcing --stat
```
| Element | Opis |
|---|---|
| `domain.CardEvent` (sealed: `CardIssued`, `TransactionRegistered`) | zdarzenia domenowe jako źródło prawdy |
| `domain.Card` | `record(event)` → `apply(event)`, `fromHistory`, `fromSnapshot`/`snapshot`, `version`; bez listy transakcji |
| `schema.sql` | `card_events` (append-only, PK `(stream_id, version)`) i `card_snapshots` |
| `adapters.persistence.EventSourcedCardRepository` | ten sam port `CardRepository`: append z oczekiwaną wersją, migawki co N zdarzeń |
| `adapters.persistence.StoredEventMapper` | jawny, wersjonowany format zapisu (`CardIssued.v1`, …) |
| `application.PaymentsEventHistory` + `EventStoreHistory` | odtworzenie całej historii (replay) |
| `queries.CardProjectionRebuilder` | odbudowa MongoDB przez replay zdarzeń tą samą projekcją co na bieżąco |

Usunięte: encje JPA kart, adapter JDBC, `Card.restore()`.

## Na co zwrócić uwagę
- `CardEventSourcingTest` pokazuje testy given (historia zdarzeń) / when (komenda) / then (nowe zdarzenia).
- **Optimistic concurrency bez `@Version`**: dwóch piszących z tą samą wersją próbuje dopisać `(stream_id, version)`, a drugi dostaje `DuplicateKeyException`, czyli `CardModifiedConcurrentlyException` (`OptimisticLockingTest` przechodzi bez zmian).
- `CardRepositoryContractTest` przechodzi dla event store, bo port dotrzymuje kontraktu niezależnie od modelu utrwalania.
- `EventStoreTest`: 25 zdarzeń, migawka po 20., odczyt = migawka + 5 zdarzeń.
- `AddCardService` i `AddTransactionService` publikują zdarzenia aplikacyjne mapowane ze zdarzeń domenowych (`PaymentsEvents.from`).
- Zapytaj H2 (`/h2`): `select * from card_events order by position` to pełna historia karty.

## Kiedy NIE stosować
Gdy historia zmian nie ma wartości biznesowej, a domena to głównie CRUD. ES to zmiana paradygmatu:
wersjonowanie zdarzeń, migracje przez upcasting, RODO (prawo do usunięcia danych w niezmiennym logu), obowiązkowe modele odczytu.

## Pytania do dyskusji
- Jak zmienić format zdarzenia zapisanego dwa lata temu (upcasting, wersje, kopiowanie strumieni)?
- Jak „cofnąć” błędną transakcję w świecie, w którym zdarzeń się nie usuwa?
- Gdzie w Waszym systemie historia zmian jest ważniejsza niż bieżący stan?

## Uruchomienie
```bash
./mvnw test                         # testy MongoDB/PostgreSQL wymagają Dockera
docker compose up -d mongo
./mvnw spring-boot:run
```

---

## Ćwiczenie „blog”, część 4 (koniec ścieżki CQRS/ES)
1. Rozdziel w blogu zapis od odczytu: komendy (utwórz, opublikuj, skomentuj) przez use case'y, a zapytania („po kategorii”, „po tagach”) jako osobny model odczytu.
2. Zbuduj model odczytu ze zdarzeń (`ArticlePublished`, `CommentAdded`, `ArticleLiked`) w osobnej kolekcji lub tabeli, idempotentnie.
3. Dla chętnych: przechowuj `Article` jako strumień zdarzeń i dodaj endpoint „stan artykułu na dzień X” (replay do daty).
