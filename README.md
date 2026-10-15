# 09 – Testowanie czystej architektury

> Moduł 2: Implementacja · poprzedni: `08-adapter-swap` · następny: `10-second-context-naive`

## Cel
Uporządkować strategię testów pod architekturę (co testujemy na którym poziomie) i zamienić zasady
architektoniczne w **wykonywalne reguły** (ArchUnit), które pilnują projektu przy każdym buildzie.

## Problem z poprzedniego kroku
Reguły architektury były zapisane głównie w README i ADR-ach. Nic nie broni się przed pośpiechem:
jeden import z `adapters` w use case i reguła zależności przestaje obowiązywać.

## Piramida testów w tym projekcie
| Poziom | Co testujemy | Narzędzia | Przykłady | Czas |
|---|---|---|---|---|
| Domena | reguły biznesowe | JUnit, AssertJ | `CardTest`, `MoneyTest`, `CurrencyExchangeTest` | ms |
| Aplikacja | orkiestracja use case'ów | fake'i portów | `AddTransactionServiceTest` | ms |
| Adapter wejściowy | kontrakt HTTP, mapowanie, błędy | `@WebMvcTest`, `@MockitoBean` | `AddTransactionRestControllerTest` | s |
| Adapter wyjściowy | kontrakt portu na prawdziwej technologii | `@DataJpaTest`, `@SpringBootTest`, Testcontainers, `MockRestServiceServer` | `CardRepositoryContractTest` (JPA, JDBC, PostgreSQL), `NbpExchangeRateAdapterTest` | s |
| Przekrojowy | cała ścieżka | `@SpringBootTest` + MockMvc | `PaymentsRestApiTest` | s |
| Architektura | reguły zależności i konwencje | ArchUnit | `ArchitectureTest` | ms |

Im bliżej domeny, tym więcej szybkich testów. Testy przekrojowe są nieliczne i sprawdzają tylko „okablowanie”.

## Co się zmieniło
```bash
git diff 08-adapter-swap..09-testing-architecture --stat
```
- `ArchitectureTest` przepisany na `@AnalyzeClasses` + `@ArchTest` i rozbudowany o reguły:
  - **warstwy**: `layeredArchitecture()` z wyjątkiem dla composition root,
  - **onion**: ta sama reguła w terminologii `onionArchitecture()`,
  - **rdzeń bez frameworków**: brak `org.springframework`, `jakarta.persistence`, Jacksona,
  - **deterministyczna domena**: zakaz `ZonedDateTime.now()` w domenie,
  - **cykle i niezależność adapterów**: `slices().beFreeOfCycles()`, `notDependOnEachOther()`,
  - **konwencje**: `*UseCase` to interfejsy, `*Service` je implementuje, kontrolery i encje są ukryte w adapterach, `*Adapter` ma `@Adapter`.
- Adaptery z pakietu głównego przeniesione do `adapters.events`, `adapters.time` i `adapters.generator`. Reguła onion wymusiła ten porządek.
- `PostgresCardRepositoryContractTest`: kontrakt portu na PostgreSQL w Testcontainers (pomijany bez Dockera).

## Na co zwrócić uwagę
- Spróbuj złamać regułę: dodaj w `AddTransactionService` import klasy z `adapters` i uruchom `./mvnw test`.
- `.because(...)` w regule to dokumentacja, która pojawi się w komunikacie błędu.
- W istniejącym projekcie `FreezingArchRule.freeze(rule)` zapisuje obecne naruszenia i blokuje tylko nowe, co pozwala wprowadzać reguły stopniowo.
- Alternatywa: jMolecules (`@AggregateRoot`, `@ValueObject`, `@DomainLayer`) + `jmolecules-archunit` wyrażają role DDD adnotacjami i weryfikują je automatycznie.

## Kiedy NIE stosować
Reguły ArchUnit to kod do utrzymania. Zaczynaj od kilku reguł chroniących najważniejsze decyzje
(kierunek zależności, czysty rdzeń), a nie od listy konwencji nazewniczych.

## Pytania do dyskusji
- Które z tych reguł chcielibyście mieć w swoim projekcie od jutra?
- Testy adapterów na H2 czy na prawdziwej bazie? Ile kosztuje Testcontainers w CI?
- Czy mockować use case w teście kontrolera, czy testować całą ścieżkę?

---

## Ćwiczenie „blog”, część 2 (koniec modułu 2)
Rozbuduj bloga z części 1 o adaptery:
1. Spring Boot jako composition root (`@Configuration`), use case'y bez adnotacji Springa.
2. REST API z osobnymi DTO, walidacją i mapowaniem wyjątków na Problem Details.
3. Persystencja JPA (H2) z osobnym modelem. Zapytania „po kategorii” i „po tagach” zrealizuj w adapterze. Podpowiedź: JPQL `join a.tags t where t.name in :tags group by a having count(a) = :count`.
4. Testy: `@WebMvcTest` dla jednego kontrolera, test kontraktu repozytorium, `ArchitectureTest` z regułą warstw i „rdzeń bez Springa”.

## Uruchomienie
```bash
./mvnw test     # testy Testcontainers wymagają uruchomionego Dockera
```
