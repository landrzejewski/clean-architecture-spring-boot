# 08 – Wymienność adapterów

> Moduł 2: Implementacja · poprzedni: `07-persistence-adapter` · następny: `09-testing-architecture`

## Cel
Udowodnić w praktyce obietnicę portów i adapterów. Trzy zmiany:
- nowa technologia persystencji,
- nowe zewnętrzne API,
- nowy kanał wejściowy.

Każda z nich to **nowy adapter**. Domena i use case'y zmieniają się tylko wtedy, gdy zmienia się wymaganie biznesowe.

## Problem z poprzedniego kroku
Wracamy do ćwiczenia z `00`: prowizja 2% za płatność w obcej walucie, kurs z zewnętrznego źródła.
Porównaj, ile to kosztowało w `CardService`, a ile kosztuje teraz.

## Co się zmieniło
```bash
git diff 07-persistence-adapter..08-adapter-swap --stat
```
| Zmiana | Port | Adapter(y) |
|---|---|---|
| Druga persystencja | `CardRepository` | `JpaCardRepositoryAdapter` (domyślny, optimistic locking) / `JdbcCardRepositoryAdapter` (profil `jdbc`, `SELECT … FOR UPDATE`) |
| Kursy walut | `ExchangeRateProvider` (nowy) | `FixedExchangeRateAdapter` (domyślny, offline) / `NbpExchangeRateAdapter` (`payments.exchange-rates.provider=nbp`, API NBP) |
| Nowy kanał wejściowy | `AddTransactionUseCase` | `CsvTransactionImporter` + `CsvImportScheduler` (`payments.import.enabled=true`) |
| Reguła biznesowa | – | domena: `ExchangeRate` (VO), `CurrencyExchange` (usługa domenowa, prowizja 2%) |

## Na co zwrócić uwagę
- `NbpExchangeRateAdapter` to **Anti-Corruption Layer**. Rekordy `NbpRateTable`/`NbpRate` (model NBP) nie wychodzą poza adapter. Aplikacja dostaje domenowy `ExchangeRate`.
- `CardRepositoryContractTest` to jeden zestaw testów kontraktu portu uruchamiany dla JPA i JDBC (`Jpa…ContractTest`, `Jdbc…ContractTest`).
- JDBC wymaga ręcznej pracy: mapowania wierszy, relacji, wykrywania nowych transakcji i blokad. Za ten kod płacimy, rezygnując z ORM.
- Dwa adaptery mają dwie różne strategie współbieżności, ale ten sam kontrakt portu.
- Nowe wymaganie biznesowe trafiło do domeny (`CurrencyExchange`) i jest testowane bez sieci (`CurrencyExchangeTest`, `AddTransactionServiceTest`).
- `NbpExchangeRateAdapterTest` testuje ACL bez sieci (`MockRestServiceServer`).

## Kiedy NIE stosować
Wymienność ma sens tam, gdzie jest realna potrzeba: testy, różne środowiska, migracja technologii, wiele kanałów.
Abstrakcja „na zapas” nad każdą biblioteką to koszt bez zysku.

## Pytania do dyskusji
- Czy `ExchangeRateProvider` powinien być portem aplikacji czy domeny?
- Co zrobić, gdy NBP nie odpowiada? Retry, cache, fallback do ostatniego kursu? Gdzie ta logika powinna żyć?
- Czy import CSV powinien wywoływać use case per linia czy osobny use case „import wsadowy”?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run -Dspring-boot.run.arguments=--payments.exchange-rates.provider=nbp
./mvnw spring-boot:run -Dspring-boot.run.profiles=jdbc
./mvnw spring-boot:run -Dspring-boot.run.arguments=--payments.import.enabled=true   # pliki *.csv w katalogu import/
```
