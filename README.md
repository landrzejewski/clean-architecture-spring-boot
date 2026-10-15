# 01 – Bogaty model domeny

> Moduł 1: Dlaczego architektura · poprzedni: `00-layered-anemic` · następny: `02-use-cases-and-ports`

## Cel
Przenieść reguły biznesowe z serwisu (Transaction Script) do modelu domeny, który sam pilnuje swojej poprawności.
Model powstaje w **czystej Javie**: bez Springa, JPA i bazy danych.

## Problem z poprzedniego kroku
W `00` reguły (saldo, waluta, data ważności) siedziały w `CardService`, encja była workiem na dane,
a test jednej reguły wymagał kontekstu Springa.

## Co się zmieniło
```bash
git diff 00-layered-anemic..01-rich-domain --stat
```
Kod z `00` został usunięty. Zostaje sam model w pakiecie `payments.domain`:

| Element DDD | Klasa | Uwagi |
|---|---|---|
| Agregat | `Card` | jedyny punkt zmiany salda, pilnuje niezmienników, brak setterów |
| Value Object | `Money`, `CardNumber`, `CardId`, `TransactionId` | rekordy, walidacja w konstruktorze, porównanie po wartości |
| Encja (w agregacie) | `Transaction` | ma tożsamość (`TransactionId`) |
| Zdarzenie domenowe | `TransactionRegistered` | zbierane w agregacie i odbierane przez `pullEvents()` |
| Wyjątki domenowe | `InsufficientBalanceException`, … | nazwy z języka biznesu zamiast `IllegalStateException` |

## Na co zwrócić uwagę
- `Card.registerTransaction()` najpierw waliduje, potem zmienia stan, a na końcu rejestruje zdarzenie. Nie da się „przypadkiem” ustawić salda.
- `Card.restore()` odtwarza agregat z zapisanego stanu bez walidacji i bez zdarzeń. Przyda się w adapterze persystencji (`07`).
- `Money` normalizuje skalę, więc `10 PLN == 10.00 PLN`. Operacje na różnych walutach rzucają wyjątek domenowy.
- Testy (`CardTest`, `MoneyTest`) wykonują się w milisekundach i czytają się jak specyfikacja.

## Model anemiczny vs bogaty
| | Anemiczny (`00`) | Bogaty (`01`) |
|---|---|---|
| Gdzie są reguły | w serwisach | w agregatach i VO |
| Ochrona niezmienników | brak (settery) | enkapsulacja |
| Testy | integracyjne | jednostkowe, szybkie |
| Język | techniczny (`"IN"`, `BigDecimal`) | domenowy (`Money`, `INFLOW`) |

## Kiedy NIE stosować
Gdy logiki prawie nie ma (CRUD, formularze, raporty). Bogaty model kosztuje: mapowanie, więcej klas, dyscyplinę zespołu.

## Pytania do dyskusji
- Gdzie przebiega granica agregatu? Czy `Transaction` powinna być osobnym agregatem?
- Kto powinien generować `TransactionId` i znacznik czasu: domena czy warstwa wyżej?
- Czy `Card` powinna trzymać pełną listę transakcji? Co przy milionie transakcji? Wrócimy do tego w CQRS/ES.

## Uruchomienie
```bash
./mvnw test
```
