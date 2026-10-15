# 07 – Adapter persystencji i mapowanie modeli

> Moduł 2: Implementacja · poprzedni: `06-rest-adapter` · następny: `08-adapter-swap`

## Cel
Zastąpić repozytorium in-memory adapterem JPA **bez zmiany domeny i use case'ów**, z osobnym modelem
persystencji, transakcją na granicy use case'u i obsługą współbieżności (optimistic locking).

## Problem z poprzedniego kroku
Dane znikały po restarcie. Najprostsze rozwiązanie z `00`, czyli adnotacje JPA na modelu domeny, zniszczyłoby
enkapsulację agregatu i uzależniło domenę od `jakarta.persistence`.

## Co się zmieniło
```bash
git diff 06-rest-adapter..07-persistence-adapter --stat
```
| Element | Klasa |
|---|---|
| Model persystencji | `adapters/persistence/CardEntity`, `TransactionEntity` (package-private) |
| Spring Data | `SpringDataCardRepository` |
| Mapper domena ↔ JPA | `JpaCardMapper` |
| Adapter portu | `JpaCardRepositoryAdapter` (`@Transactional(MANDATORY)`, tłumaczenie wyjątków) |
| Wyjątek portu | `application/CardModifiedConcurrentlyException` + `@Retry` w `AddTransactionService` |
| Transakcje do odczytu | `@Atomic(readOnly = true)` na `GetCard*Service` |
| PostgreSQL | profil `postgres` + `docker-compose.yml` |

Usunięte: `HashMapCardRepository`. Testy use case'ów nadal używają fake'a `InMemoryCardRepository`.

## Strategie mapowania między warstwami
| Strategia | Opis | Gdzie w repo |
|---|---|---|
| Brak mapowania | ten sam model w każdej warstwie | `00-layered-anemic` (`CardEntity` jako JSON i tabela) |
| Dwukierunkowe (two-way) | każda warstwa ma model, mapujemy tam i z powrotem | `JpaCardMapper` (domena ↔ JPA) |
| Pełne (full) | osobny model wejściowy dla każdej operacji | `AddTransactionRequest` → `AddTransactionCommand` (`06`) |
| Jednokierunkowe (one-way) | model wyjściowy tylko do odczytu, budowany z domeny | `CardView` → `CardResponse` |

Wybór zależy od operacji: dla odczytów często wystarczy brak mapowania albo projekcja (zobacz `17-cqrs-light`).

## Na co zwrócić uwagę
- `JpaCardMapper.update()` aktualizuje **zarządzaną** encję i dodaje tylko nowe transakcje, a Hibernate pilnuje `@Version`.
- `saveAndFlush()` w adapterze sprawia, że konflikt wersji pojawia się w adapterze, więc możemy go przetłumaczyć na wyjątek portu.
- `OptimisticLockingTest` symuluje dwie równoległe transakcje i oczekuje `CardModifiedConcurrentlyException`.
- `JpaCardRepositoryAdapterTest` (`@DataJpaTest`) testuje kontrakt portu na prawdziwym JPA.

## Kiedy NIE stosować
Osobny model persystencji kosztuje. Gdy domena jest prosta, adnotacje JPA na encjach domenowych
to rozsądny kompromis („no mapping”). Ważne, żeby była to świadoma decyzja (ADR `0004`).

## Pytania do dyskusji
- Optimistic czy pessimistic locking dla salda karty? Co przy bardzo dużym ruchu na jednej karcie?
- Czy `CardEntity.balance` (denormalizacja) to dobry pomysł? Kto pilnuje spójności?
- Kto powinien otwierać transakcję: kontroler, use case czy adapter?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run                                      # H2 w pamięci
docker compose up -d && ./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```
