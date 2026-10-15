# 12 – Moduły Maven

> Moduł 3: Modularyzacja · poprzedni: `11-language-encapsulation` · następny: `13-jpms`

## Cel
Przenieść granice z testów ArchUnit do **systemu budowania**. Moduł, który nie deklaruje zależności,
nie ma klas na classpath, więc niedozwolony import po prostu się nie kompiluje.

## Problem z poprzedniego kroku
W `11` jedynym strażnikiem granicy „orders → tylko payments.api” był test. Kompilator pozwalał na wszystko,
bo `payments.domain` i `payments.application` muszą być `public`.

## Struktura
```
shop (pom)
├── common         adnotacje, aspekty, stronicowanie, narzędzia web
├── payments-api   PaymentsApi, PaymentRequest, PaymentResult (zero zależności)
├── payments       domain + application + adapters płatności   → common, payments-api
├── orders         domain + application + adapters zamówień    → common, payments-api
└── app            Application (composition root) + testy E2E  → payments, orders
```
`orders` **nie zależy** od `payments`. Spróbuj dodać w `orders` pole typu `pl.fullstackdeveloper.payments.domain.Card`:
```
[ERROR] package pl.fullstackdeveloper.payments.domain does not exist
```

## Co się zmieniło
```bash
git diff 11-language-encapsulation..12-maven-modules --stat -M
```
- Kod przeniesiony do modułów, bez zmian w treści klas.
- Każdy moduł jest testowany **w izolacji**:
  - `payments` ma testową aplikację `PaymentsTestApplication`,
  - `orders` testuje use case z fake'iem `PaymentsApi` (`PlaceOrderServiceTest`), a implementacji płatności nie ma nawet na classpath,
  - `common` ma `CommonTestApplication` dla testów aspektów.
- Testy przekrojowe (`OrdersIntegrationTest`, `ContextBoundariesTest`) przeniesione do `app`.
- Pakiet `compare` z modułu 1 usunięty. Porównanie architektur znajdziesz w `03-architectures-compared`.

## Na co zwrócić uwagę
- `payments-api` to osobny artefakt. W przyszłości może go używać zdalny klient (`16`).
- `app` to jedyne miejsce, które widzi wszystko. Tu jest `spring-boot-maven-plugin` i konfiguracja (`application.yml`).
- Granulacja: jeden moduł na kontekst, a nie na warstwę. Warstwy wewnątrz kontekstu dalej pilnuje ArchUnit (`payments/…/ArchitectureTest`).
- `payments` nadal może sięgnąć do wnętrza `orders`? Nie, bo nie ma go w zależnościach. Kierunek jest wymuszony.

## Kiedy NIE stosować
Każdy moduł Maven zwiększa czas buildu i liczbę plików konfiguracji. Moduł per warstwa (`domain`, `application`, `adapters`)
w każdym kontekście szybko daje kilkanaście modułów, więc warto zacząć od modułu per kontekst.

## Pytania do dyskusji
- Moduł per kontekst czy per warstwa? A może oba (np. `payments-domain` bez Springa)?
- Co powinno trafić do `common`, a co jest „shared kernel”, który wiąże konteksty?
- Jak wersjonować `payments-api`, gdy korzysta z niego wiele zespołów?

## Uruchomienie
```bash
./mvnw test
./mvnw -DskipTests package && java -jar app/target/app-1.0-SNAPSHOT.jar
```
