# 06 – Adapter REST

> Moduł 2: Implementacja · poprzedni: `05-spring-as-detail` · następny: `07-persistence-adapter`

## Cel
Wystawić use case'y przez HTTP jako **adapter wejściowy**: osobny kontrakt (DTO), walidacja wejścia,
mapowanie na komendy i tłumaczenie wyjątków na odpowiedzi HTTP (Problem Details, RFC 9457).

## Problem z poprzedniego kroku
Use case'y działały tylko z `DemoRunner`. W `00` kontroler zwracał encje JPA, więc kontrakt REST był
sklejony ze schematem bazy. Tym razem kontrakt REST ma być niezależny od domeny.

## Co się zmieniło
```bash
git diff 05-spring-as-detail..06-rest-adapter --stat
```
| Element | Klasa |
|---|---|
| Kontrolery (jeden na use case) | `adapters/rest/*RestController` |
| Kontrakt REST | `RestModel.java`: `AddTransactionRequest`, `CardResponse`, `TransactionTypeDto`, … |
| Mapowanie DTO ↔ komenda / model wyjściowy | `CardRestMapper` |
| Wyjątki → HTTP | `PaymentsExceptionHandler` (`ProblemDetail`, i18n z `exceptions*.properties`) |
| Żądania do ręcznego testowania | `client.http` |

## Na co zwrócić uwagę
- **Trzy poziomy walidacji**:
  - Bean Validation w DTO sprawdza format (`[A-Z]{3}`, `@Positive`).
  - Komenda sprawdza kompletność.
  - Domena sprawdza reguły biznesowe.

  Każdy poziom ma inną odpowiedzialność.
- `TransactionTypeDto` (`IN`/`OUT`) to język API, a `TransactionType` (`INFLOW`/`PAYMENT`) to język domeny. Mapper tłumaczy jeden na drugi.
- Domena nie zna kodów HTTP. Decyzja „brak środków = 422” należy do adaptera.
- Testy:
  - `AddTransactionRestControllerTest` (`@WebMvcTest` + `@MockitoBean` portu wejściowego) testuje sam adapter.
  - `PaymentsRestApiTest` przechodzi przez całą aplikację.

## Kiedy NIE stosować
Kontroler per use case daje dużo małych klas. Przy kilku endpointach jeden kontroler per zasób jest równie dobry.
Osobne DTO dla prostego CRUD-a to często nadmiar.

## Pytania do dyskusji
- Czy kontroler może używać typów domenowych (`CardNumber`, `Money`)? Jakie są granice?
- 400 czy 422 dla błędów biznesowych? Co mówi RFC 9110?
- Gdzie mapować: w kontrolerze, w mapperze adaptera czy w use case?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run   # potem client.http
```
