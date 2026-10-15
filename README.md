# 17 – CQRS „lekki” (jedna baza, dwa modele)

> Moduł 4: Wzorce zaawansowane · bazuje na: `15-spring-modulith` · następny: `18-cqrs-read-store`

## Cel
Rozdzielić stronę **zapisu** (komendy → use case → agregat → repozytorium) od strony **odczytu**
(zapytania → projekcja prosto z bazy). Na razie bez drugiej bazy i bez zdarzeń: te same tabele, dwa różne modele.

## Problem z poprzedniego kroku
- `GetCardService` ładował **cały agregat z historią transakcji** tylko po to, żeby pokazać saldo i liczbę transakcji.
- Port `CardRepository` rósł o metody potrzebne wyłącznie ekranom (`findAll` ze stronicowaniem).
- Nowe widoki (np. lista transakcji karty) wymagałyby kolejnych metod w porcie zapisu.

## CQS → CQRS
- **CQS** (B. Meyer): metoda albo zmienia stan (command), albo zwraca dane (query), nigdy jedno i drugie.
- **CQRS** (G. Young, U. Dahan): ta sama zasada na poziomie modeli i komponentów. Osobny model zapisu (bogata domena, niezmienniki) i osobny model odczytu (płaskie DTO, zapytania optymalizowane pod widok).

## Co się zmieniło
```bash
git diff 15-spring-modulith..17-cqrs-light --stat
```
| Strona | Elementy |
|---|---|
| Szyna | `common.cqrs`: `Command<R>`, `Query<R>`, `CommandHandler`, `QueryHandler`, `Bus`, `SpringBus` |
| Zapis | `AddCardUseCase` / `AddTransactionUseCase` **są** `CommandHandler`-ami; komendy implementują `Command<R>` |
| Zapis | `CardRepository` bez `findAll`: port zapisu potrzebuje tylko `findByNumber` i `save` |
| Odczyt | `payments.queries`: `GetCardSummaryQuery`, `GetCardsQuery`, `GetCardTransactionsQuery` + handlery na `JdbcClient` |
| REST | kontrolery wysyłają komendy (`bus.send`) i zadają pytania (`bus.ask`); kontrakt JSON bez zmian |
| Nowość | `GET /api/cards/{number}/transactions`, odczyt niedostępny wcześniej bez ładowania agregatu |

Usunięte: `GetCardUseCase`, `GetCardsUseCase`, `CardView` i ich serwisy.

## Na co zwrócić uwagę
- `ArchitectureTest.read_side_is_independent_of_write_model`: strona odczytu nie zna `domain`, `application` ani `adapters`.
- Liczba transakcji jest liczona w SQL (`count(*)`). Agregat nie jest ładowany.
- `SpringBus` wyszukuje handler po typie generycznym komunikatu. To ta sama idea co *mediator*.
- `CardResponse` (REST) nie zmienił się. Klienci nie wiedzą, że pod spodem jest CQRS.
- Zapytania z ćwiczenia „blog” (`findByCategory`, `findByTags` z `select new …View`) to dokładnie ten wzorzec: projekcja odczytowa obok modelu zapisu.

## Kiedy NIE stosować
- Gdy odczyty i zapisy są symetryczne (CRUD), osobne modele to dublowanie kodu.
- Szyna (`Bus`) dodaje pośrednictwo i utrudnia nawigację w IDE. Kontroler może wołać handler bezpośrednio, a CQRS dalej działa.

## Pytania do dyskusji
- Czy strona odczytu może łamać enkapsulację modelu zapisu (czytać jego tabele)? Co, gdy schemat się zmieni?
- Czy komenda powinna cokolwiek zwracać (id)? Jak to się ma do „czystego” CQS?
- Kiedy jedna baza przestaje wystarczać? (`18`)

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run
```
