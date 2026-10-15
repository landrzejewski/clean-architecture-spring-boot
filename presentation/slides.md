---
marp: true
theme: training
paginate: true
lang: pl
title: Czysta architektura w praktyce
header: Czysta architektura w praktyce
footer: Łukasz Andrzejewski · szkolenie Clean Architecture
---

<!-- _class: lead -->
<!-- _paginate: false -->
<!-- _header: "" -->
<!-- _footer: "" -->

# Czysta architektura w praktyce

Od aplikacji warstwowej do modularnego monolitu i mikroserwisów<br>CQRS · Event Sourcing · Saga

**Łukasz Andrzejewski**

---

# Plan szkolenia

| Moduł | Temat | Branche |
|---|---|---|
| 1 | Dlaczego architektura: warstwy, model domeny, porty i adaptery, porównanie stylów | `00`–`03` |
| 2 | Implementacja: zagadnienia przekrojowe, Spring jako szczegół, adaptery REST i persystencji, testy architektury | `04`–`09` |
| 3 | Modularyzacja: hermetyzacja, moduły Maven, JPMS, komunikacja, Spring Modulith, mikroserwis | `10`–`16` |
| 4 | Wzorce zaawansowane: CQRS, osobny model odczytu, Event Sourcing, saga | `17`–`21` |

Każdy moduł kończy się ćwiczeniem na przykładzie **bloga**.

---

# Jak pracujemy z repozytorium

- Jedna domena przez całe szkolenie: **karty i płatności**, potem **zamówienia** i **wysyłki**.
- Każdy branch to **jeden koncept** zbudowany na poprzednim: `git diff 05-spring-as-detail..06-rest-adapter`
- Na każdym branchu `README.md`: cel, problem, zmiany, kiedy **nie** stosować, pytania.
- Decyzje w `docs/adr`, model C4 w `docs/c4`, a każda reguła ma test (`./mvnw test`).

```
00 → 01 → … → 15 ─┬─► 16 (mikroserwis)
                  ├─► 17 → 18 → 19 (CQRS, ES)
                  └─► 20 → 21 (saga)
```

---

<!-- _class: lead -->

# Moduł 1
## Dlaczego architektura

<span class="branch">00</span> <span class="branch">01</span> <span class="branch">02</span> <span class="branch">03</span>

---

# Podstawowe pojęcia

- **Reguły biznesowe** definiują pojęcia i polityki niezbędne do działania biznesu.
- **Proces biznesowy** to seria powtarzalnych kroków prowadzących do celu biznesowego.
- **Logika biznesowa** to część aplikacji realizująca przyjęte reguły biznesowe.
- **Architektura** określa najważniejsze komponenty, ich odpowiedzialność i wzajemne relacje. Jest szablonem rozwiązania na poziomie aplikacji lub systemu.

> Poprawna implementacja logiki biznesowej sprowadza się zwykle do dwóch rzeczy: **niskiego sprzężenia** (low coupling) i **wysokiej spójności** (high cohesion).

---

# Skutki źle dobranej architektury

- Każda zmiana „rozlewa się” po wielu klasach i warstwach.
- Reguły biznesowe są ukryte w kontrolerach, serwisach, zapytaniach SQL i triggerach.
- Testy są wolne, kruche i wymagają całej infrastruktury.
- Wymiana technologii (bazy, kolejki, frameworka) oznacza przepisanie logiki.
- Zespół boi się zmian, a koszt każdej funkcji rośnie z czasem.

**Wymagania niefunkcjonalne** (dostępność 24/7, 1000 równoczesnych użytkowników, 200 transakcji na minutę)
również kształtują architekturę. Wrócimy do nich przy CQRS i mikroserwisach.

---

# <span class="branch">00-layered-anemic</span> Punkt startowy

<div class="columns">
<div>

```java
@Service @Transactional
public class CardService {
  public CardEntity addTransaction(String number,
      BigDecimal amount, String currency, String type) {
    var card = repo.findByNumber(number).orElseThrow();
    if (!card.getCurrencyCode().equals(currency)) …
    if (type.equals("OUT")) {
      if (card.getBalance().compareTo(amount) < 0) …
      card.setBalance(card.getBalance().subtract(amount));
    } …
```

</div>
<div>

- `Controller → Service → Repository`
- encja JPA = model domeny = kontrakt REST
- logika w `if`-ach serwisu (**Transaction Script**)
- `ZonedDateTime.now()`, `Random`, `System.out` w logice
- test reguły wymaga `@SpringBootTest`

**Ćwiczenie:** dodaj 2% prowizji za płatność w obcej walucie.

</div>
</div>

---

# Model anemiczny vs bogaty

| | Anemiczny | Bogaty (DDD) |
|---|---|---|
| Encje | struktury danych (gettery/settery) | zachowania + niezmienniki |
| Logika | serwisy (Transaction Script) | agregaty, encje, Value Objects |
| Repozytoria | DAO dla każdej tabeli | dla agregatów (jest ich mniej) |
| Serwisy | „grube” | lekkie, orkiestrujące |
| Testy | integracyjne | jednostkowe, w milisekundach |

**Domain Driven Design** zakłada, że obiekty i ich zachowania wiernie odwzorowują domenę problemu.

---

# <span class="branch">01-rich-domain</span> Agregat i Value Objects

<div class="columns">
<div>

```java
public final class Card {

  public void registerTransaction(Transaction t) {
    validate(t);      // waluta, ważność, saldo
    commit(t);        // zmiana salda
    events.add(new TransactionRegistered(number, t));
  }

  public List<TransactionRegistered> pullEvents() { … }
}

public record Money(BigDecimal amount, Currency currency) {
  public Money {
    if (amount.signum() < 0) throw …;
    amount = amount.setScale(…);  // 10 == 10.00
  }
}
```

</div>
<div>

- **Agregat** to granica spójności: stan zmienia się tylko przez metody biznesowe.
- **Value Object**: niezmienny, porównywany po wartości, waliduje się sam.
- **Zdarzenie domenowe**: fakt w czasie przeszłym.
- Czysta Java: bez Springa, JPA i bazy.

</div>
</div>

---

# Kontrakty, IoC i wstrzykiwanie zależności

- **Kontrakt (interfejs, API)** to abstrakcja definiująca sposób interakcji współpracujących elementów i ukrywająca szczegóły implementacji.
- **Inwersja kontroli (IoC)** przenosi odpowiedzialność za sterowanie wykonaniem na zewnątrz.
- **Wstrzykiwanie zależności**: zależności są tworzone i podawane z zewnątrz:
  - ręcznie (konstruktor), <span class="branch">02</span>
  - wzorce, np. Factory, <span class="branch">04</span>
  - kontener (Spring, CDI). <span class="branch">05</span>
- **DIP**: moduły wysokiego poziomu nie zależą od niskiego poziomu. Obie strony zależą od abstrakcji, a abstrakcję definiuje **strona, która jej potrzebuje**.

---

# <span class="branch">02-use-cases-and-ports</span> Porty i adaptery

<div class="flow">
<div class="box">REST / CLI / main()</div><div class="arrow">→</div>
<div class="box">port wejściowy<br><code>AddTransactionUseCase</code></div><div class="arrow">→</div>
<div class="box">use case<br><code>AddTransactionService</code></div><div class="arrow">→</div>
<div class="box">domena<br><code>Card</code></div>
</div>

<div class="flow" style="margin-top:14px">
<div class="box">use case</div><div class="arrow">→</div>
<div class="box">porty wyjściowe<br><code>CardRepository</code> · <code>DateTimeProvider</code> · <code>TransactionEventPublisher</code></div><div class="arrow">←</div>
<div class="box">adaptery<br>in-memory, konsola</div>
</div>

- Komendy walidują się same (`AddTransactionCommand`).
- Model wyjściowy (`CardView`) chroni agregat przed wyciekiem.
- Composition root w `main()`: DI bez frameworka.
- Test use case'u z fake'ami portów, z kontrolą czasu.

---

# Architektura warstwowa (n-tier)

- Komponenty zorganizowane w warstwy techniczne: prezentacja, logika, dane.
- Komunikacja w jednym kierunku, z góry w dół.
- **Logika zależy od persystencji.** Zmiana bazy dotyka reguł biznesowych.
- Prosta, znana i wystarczająca dla aplikacji z małą ilością logiki.

<div class="flow" style="margin-top:20px">
<div class="box">Presentation</div><div class="arrow">↓</div>
<div class="box">Business</div><div class="arrow">↓</div>
<div class="box warn">Persistence</div>
</div>

<p class="small">herbertograca.com/2017/08/03/layered-architecture</p>

---

# Czysta architektura

<div class="columns">
<div>

<div class="rings">
  <div class="r4">Frameworks &amp; Drivers</div>
  <div class="r3">Interface Adapters</div>
  <div class="r2">Use Cases</div>
  <div class="r1">Entities</div>
</div>

</div>
<div>

- W centrum logika biznesowa, niezależna od bibliotek, frameworków i infrastruktury.
- Wokół niej kolejne warstwy, m.in. adaptery łączące ze światem zewnętrznym.
- **Reguła zależności**: zależności w kodzie wskazują wyłącznie do środka.
- Przepływ sterowania może iść na zewnątrz (presenter), ale zależność dalej wskazuje do środka (DIP).

<p class="small">blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html</p>

</div>
</div>

---

# <span class="branch">03-architectures-compared</span> Jeden use case, cztery style

| | Warstwowa | Onion | Heksagonalna | Czysta |
|---|---|---|---|---|
| Kierunek zależności | w dół, do bazy | do środka | do środka | do środka |
| Interfejs repozytorium | warstwa danych | domain services | port wyjściowy | gateway w use cases |
| Akcent | podział techniczny | pierścienie modelu | porty i wymienne adaptery | granice, presenter |
| Reguła ArchUnit | `layeredArchitecture()` | `onionArchitecture()` | `noClasses()…` | warstwy-pierścienie |

Onion, heksagonalna i czysta różnią się głównie nazewnictwem. **Reguła zależności jest ta sama.**

**Ćwiczenie „blog”, część 1:** domena i warstwa aplikacji bloga w czystej Javie.

---

<!-- _class: lead -->

# Moduł 2
## Implementacja czystej architektury

<span class="branch">04</span> <span class="branch">05</span> <span class="branch">06</span> <span class="branch">07</span> <span class="branch">08</span> <span class="branch">09</span>

---

# Programowanie aspektowe

- Uzupełnia OOP: oddziela logikę biznesową od zadań pobocznych (transakcje, logowanie, bezpieczeństwo, pomiar czasu).
- Najczęściej realizowane wzorcem **Proxy**.

<div class="columns">
<div>

<span class="branch">04-cross-cutting-by-hand</span>

- Decorator: osobna klasa per interfejs
- JDK dynamic proxy + łańcuch interceptorów
- `@Atomic`, `@Timer` deklarują intencję bez technologii
- Factory + Facade = composition root

</div>
<div>

<span class="branch">05-spring-as-detail</span>

- `@Configuration` = composition root
- Spring AOP zamiast własnych proxy
- `AspectOrder`: retry → timer → atomic
- ArchUnit: rdzeń bez `org.springframework..`

</div>
</div>

---

# Spring jako szczegół

```java
@Configuration
public class PaymentsConfiguration {

  @Bean
  public AddTransactionUseCase addTransactionUseCase(DateTimeProvider clock,
      TransactionEventPublisher publisher, CardRepository cards) {
    return new AddTransactionService(clock, publisher, cards);  // zwykła klasa
  }
}
```

- Domena i use case'y **bez adnotacji Springa**. Testy z modułu 1 działają bez zmian.
- Adaptery mogą znać framework (`@Adapter` = `@Component`).
- Pułapki proxy: self-invocation, metody `public`, klasy nie-`final`.

> Kompromis: wiele zespołów świadomie akceptuje `@Transactional` w warstwie aplikacji. Ważne, żeby to była decyzja (ADR), a nie przypadek.

---

# <span class="branch">06-rest-adapter</span> Adapter wejściowy REST

<div class="columns">
<div>

```java
@PostMapping("api/cards/{number}/transactions")
ResponseEntity<TransactionIdResponse> addTransaction(
    @PathVariable String number,
    @Validated @RequestBody AddTransactionRequest request) {
  var id = addTransaction.handle(mapper.toCommand(number, request));
  return ResponseEntity.created(fromRequest(id)).body(…);
}
```

</div>
<div>

- Osobny kontrakt REST (DTO), niezależny od domeny.
- Trzy poziomy walidacji: format (DTO), kompletność (komenda), reguły (domena).
- `IN`/`OUT` (API) ↔ `INFLOW`/`PAYMENT` (domena).
- Wyjątki → **Problem Details** (RFC 9457), i18n.
- `@WebMvcTest` + `@MockitoBean` portu.

</div>
</div>

---

# <span class="branch">07-persistence-adapter</span> Persystencja i mapowanie

- Model JPA (`CardEntity`) oddzielony od agregatu i ukryty w adapterze (package-private).
- Agregat odtwarzany przez `Card.restore()` bez walidacji i bez zdarzeń.
- Transakcję otwiera use case (`@Atomic`), adapter jej wymaga (`MANDATORY`).
- Optimistic locking (`@Version`) → wyjątek **portu** `CardModifiedConcurrentlyException` → `@Retry`.

| Strategia mapowania | Przykład w repo |
|---|---|
| brak mapowania | `00` (encja wszędzie) |
| dwukierunkowe | `JpaCardMapper` |
| pełne (model per operacja) | `AddTransactionRequest` → `AddTransactionCommand` |
| jednokierunkowe | `CardView` → `CardResponse` |

---

# <span class="branch">08-adapter-swap</span> Wymienność adapterów

| Zmiana | Port | Adaptery |
|---|---|---|
| druga baza | `CardRepository` | JPA (`@Version`) / JDBC (`FOR UPDATE`) |
| zewnętrzne API | `ExchangeRateProvider` | stałe kursy / **NBP** (Anti-Corruption Layer) |
| nowy kanał | `AddTransactionUseCase` | REST / import CSV |

- `CardRepositoryContractTest`: ten sam zestaw testów dla każdej implementacji portu.
- Wymaganie z `00` (prowizja 2%) trafia do domeny jako usługa domenowa `CurrencyExchange`, testowana bez sieci.

---

# <span class="branch">09-testing-architecture</span> Piramida testów

| Poziom | Narzędzia | Przykład |
|---|---|---|
| domena | JUnit, AssertJ | `CardTest` |
| aplikacja | fake'i portów | `AddTransactionServiceTest` |
| adapter wejściowy | `@WebMvcTest` | `AddTransactionRestControllerTest` |
| adapter wyjściowy | `@DataJpaTest`, Testcontainers, `MockRestServiceServer` | `CardRepositoryContractTest`, `NbpExchangeRateAdapterTest` |
| przekrojowy | `@SpringBootTest` + MockMvc | `PaymentsRestApiTest` |
| architektura | ArchUnit | `ArchitectureTest` |

Im bliżej domeny, tym więcej i szybciej.

---

# Architektura jako kod (ArchUnit)

```java
@ArchTest
static final ArchRule core_is_framework_free = noClasses()
    .that().resideInAnyPackage("..domain..", "..application..")
    .should().dependOnClassesThat()
    .resideInAnyPackage("org.springframework..", "jakarta.persistence..");

@ArchTest
static final ArchRule adapters_are_independent = slices()
    .matching("..payments.adapters.(*)..").should().notDependOnEachOther();
```

- Reguły warstw, `onionArchitecture()`, deterministyczna domena (bez `now()`), brak cykli, konwencje.
- `FreezingArchRule` pozwala wprowadzać reguły w istniejącym projekcie stopniowo.
- jMolecules (`@AggregateRoot`, `@ValueObject`) + `jmolecules-archunit`.

**Ćwiczenie „blog”, część 2:** adaptery REST i JPA, testy, reguły ArchUnit.

---

<!-- _class: lead -->

# Moduł 3
## Modularyzacja

<span class="branch">10</span> <span class="branch">11</span> <span class="branch">12</span> <span class="branch">13</span> <span class="branch">14</span> <span class="branch">15</span> <span class="branch">16</span>

---

# Bounded Context i style systemów

- **Bounded Context**: aplikacja podzielona na konteksty z własnym modelem (płatności, zamówienia, wysyłki). Komunikują się przez dobrze określony interfejs.
- **Monolit**: rozwijany, testowany i wdrażany jako całość. Łatwy na początku, trudny od pewnego rozmiaru.
- **Modularny monolit**: niezależne moduły z odpowiedzialnością i publicznym kontraktem, wdrażane razem.
- **SOA**: rozproszone usługi z kontraktami, które mogą współdzielić dane i są integrowane przez ESB.
- **Mikroserwisy**: niezależne usługi, które **nie współdzielą danych** i komunikują się wyłącznie przez API.

---

# <span class="branch">10-second-context-naive</span> Drugi kontekst „na skróty”

```java
// orders.application.PlaceOrderService (wersja naiwna)
var card = cardRepository.findByNumber(command.cardNumber()).orElseThrow();
var transaction = new Transaction(new TransactionId(), clock.now(), command.total(), PAYMENT);
card.registerTransaction(transaction);       // zamówienia same obciążają kartę!
cardRepository.save(card);
```

- `orders` zna wnętrze `payments`: `CardRepository`, `Card`, `Transaction`.
- Ominięty use case: brak przeliczania walut, zdarzeń i retry.
- `payments` pokazuje zamówienia karty, więc powstaje **cykl**.
- Wspólny model (`Money`, `CardNumber`) skleja konteksty.

`ContextBoundariesTest` jest celowo wyłączony. Usuń `@Disabled`.

---

# <span class="branch">11-language-encapsulation</span> Hermetyzacja środkami języka

<div class="columns">
<div>

```java
public interface PaymentsApi {
  PaymentResult pay(PaymentRequest request);
}

public sealed interface PaymentResult {
  record Completed(String paymentId) implements PaymentResult {}
  record Rejected(String reason) implements PaymentResult {}
}

switch (paymentsApi.pay(request)) {
  case Completed c -> order.markPaid(c.paymentId());
  case Rejected r -> order.reject(r.reason());
}
```

</div>
<div>

- Wąski publiczny kontrakt: pakiet `payments.api`.
- Implementacja package-private (`PaymentsApiAdapter`).
- `sealed` wymusza obsługę wszystkich przypadków.
- Własne VO zamówień: `Price`, `CardReference`.
- Jedna transakcja na moduł.
- **Ograniczenie**: Java nie ukryje podpakietów. Granicę pilnuje tylko test.

</div>
</div>

---

# <span class="branch">12-maven-modules</span> i <span class="branch">13-jpms</span>

<div class="columns">
<div>

**Moduły Maven**
```
common
payments-api   ← kontrakt
payments       → common, payments-api
orders         → common, payments-api
app            → payments, orders
```
Import `payments.domain` w `orders`: *package does not exist*.

</div>
<div>

**JPMS**
```java
open module pl.fullstackdeveloper.payments {
  requires pl.fullstackdeveloper.payments.api;
  requires spring.context;
  // brak exports: wnętrze niewidoczne
}
```
`exports` daje dostęp w kompilacji, `opens` daje refleksję. Spring Boot uruchamia się z classpath, więc JPMS działa głównie w kompilacji.

</div>
</div>

---

# <span class="branch">14-module-communication</span> Komunikacja modułów

- Zamówienia definiują **własny port** `Payments` i własny wynik `PaymentOutcome`.
- Kontrakt płatności zna tylko adapter: **Anti-Corruption Layer** (`PaymentsTranslator`).
- Ten sam use case działa z dwoma adapterami:
  - synchronicznym: `PaymentsApi.pay()`,
  - asynchronicznym: `PaymentRequested` → `PaymentProcessed`, korelacja przez `reference`.

**Programowanie przez zdarzenia** (wzorzec Observer) rozluźnia powiązania: komponenty komunikują się, choć niewiele o sobie wiedzą.

> Słaby punkt: zdarzenie w pamięci ginie przy awarii między commitem a publikacją.

---

# <span class="branch">15-spring-modulith</span> Modularny monolit ze Spring Modulith

<div class="columns">
<div>

```java
@ApplicationModule(allowedDependencies =
    {"payments::api", "common"})
package pl.fullstackdeveloper.orders;

@ApplicationModuleListener   // async + nowa tx
void on(PaymentProcessed event) { … }

ApplicationModules.of(Application.class).verify();
```

</div>
<div>

- Moduły to pakiety najwyższego poziomu, weryfikowane testem.
- **Event Publication Registry**: zdarzenie zapisane w tej samej transakcji co zmiana (**transactional outbox**) i ponawiane.
- `@ApplicationModuleTest` + `Scenario`: test modułu w izolacji.
- `Documenter`: diagramy C4 z kodu.

</div>
</div>

| | Maven | JPMS | Modulith |
|---|---|---|---|
| kiedy | kompilacja | kompilacja | testy |
| koszt | pom-y | `module-info`, refleksja | niski |

---

# Mikroserwisy: zalety i wyzwania

<div class="columns">
<div>

**Zalety**
- modularność i reużywalność
- skalowalność i optymalne zużycie zasobów
- niezawodność i wysoka dostępność
- łatwość rozwoju pojedynczej usługi

</div>
<div>

**Wyzwania**
- złożoność na poziomie makro: infrastruktura, utrzymanie, synchronizacja stanu i procesów
- koszty: ludzie, sprzęt, utrzymanie
- sieć: wydajność i awarie
- kontrakty i ich wersjonowanie

</div>
</div>

---

# <span class="branch">16-extract-microservice</span> Wydzielenie usługi

<div class="flow">
<div class="box">orders-service :8082</div><div class="arrow">⇄ Kafka (outbox) ⇄</div>
<div class="box">payments-service :8081</div>
</div>

- W zamówieniach zmieniają się **tylko adaptery**. Domena i use case'y bez zmian (`git diff` pokazuje tylko przeniesienia).
- Sync: `HttpPaymentsAdapter` + `@Retryable` + `@ConcurrencyLimit` (Spring Framework 7).
- Async: eksternalizacja zdarzeń Modulith do Kafki oraz inbox.
- Kontrakt `payments-api` + pliki `contracts/*.json` sprawdzane po obu stronach.
- Kontrakt sieciowy musi być prosty: płaski JSON zamiast hierarchii `sealed`.

**Ćwiczenie „blog”, część 3:** kontekst notifications jako moduł Modulith.

---

# Model C4

- Opisuje architekturę i ułatwia dzielenie się wiedzą o niej.
- Cztery poziomy szczegółowości:
  1. **Context**: system, użytkownicy, systemy zewnętrzne (NBP)
  2. **Container**: aplikacje i magazyny danych (`payments-service`, `orders-service`, Kafka, bazy)
  3. **Component**: moduły (`payments`, `orders`, `payments::api`)
  4. **Code**: klasy (zwykle generowane, nie rysowane)
- W repo: `docs/c4/workspace.dsl` (Structurizr DSL) aktualizowany w `02`, `10`, `14`, `15`, `16`, oraz `docs/modulith` generowane przez `Documenter`.
- Decyzje zapisujemy w `docs/adr`.

<p class="small">c4model.com · docker run -p 8080:8080 -v $(pwd)/docs/c4:/usr/local/structurizr structurizr/lite</p>

---

<!-- _class: lead -->

# Moduł 4
## Wzorce zaawansowane

<span class="branch">17</span> <span class="branch">18</span> <span class="branch">19</span> <span class="branch">20</span> <span class="branch">21</span>

---

# CQS i CQRS

- **Command Query Separation** (B. Meyer, 1986): metoda albo zmienia stan i nic nie zwraca (command), albo zwraca dane i nie zmienia stanu (query).
- **CQRS** (G. Young, U. Dahan): ta sama zasada na poziomie klas, komponentów i systemu.

| Element | Rola |
|---|---|
| Command | intencja użytkownika, np. `AddTransactionCommand` |
| Command Bus / Handler | routing, walidacja, zmiana agregatu, zapis |
| Event | zmiana, która zaszła, np. `TransactionAdded` |
| Event Handler | aktualizacja bazy odczytu |
| Read Database | model zoptymalizowany pod odczyt |

---

# <span class="branch">17-cqrs-light</span> Dwa modele, jedna baza

```java
public interface AddTransactionUseCase
        extends CommandHandler<AddTransactionCommand, TransactionId> { }

public record GetCardSummaryQuery(String cardNumber) implements Query<Optional<CardSummary>> { }

// kontroler
bus.send(new AddTransactionCommand(…));
bus.ask(new GetCardSummaryQuery(number));
```

- Strona zapisu: agregat, niezmienniki, `CardRepository` **bez** `findAll`.
- Strona odczytu: projekcje SQL (`JdbcClient`), liczenie po stronie bazy, bez ładowania agregatu.
- Nowy widok (`/transactions`) bez zmian w modelu zapisu.

---

# <span class="branch">18-cqrs-read-store</span> Osobny magazyn odczytu

<div class="flow">
<div class="box">komenda</div><div class="arrow">→</div><div class="box">agregat</div><div class="arrow">→</div>
<div class="box">baza zapisu + outbox</div><div class="arrow">→ po commicie →</div>
<div class="box">projekcja</div><div class="arrow">→</div><div class="box">MongoDB</div><div class="arrow">←</div><div class="box">zapytanie</div>
</div>

- Dokument `card_summaries`: saldo policzone z góry, transakcje osadzone.
- Projekcja **idempotentna** (deduplikacja po `transactionId`) i odporna na kolejność (upsert).
- **Spójność ostateczna**: odczyt tuż po zapisie może być nieaktualny.
- Model odczytu można odbudować w każdej chwili (`/api/admin/projections/cards/rebuild`).

---

# Event Sourcing

- W CQRS zdarzenia synchronizują bazę zapisu i odczytu.
- **Event Sourcing** idzie dalej: stan aplikacji odtwarzamy ze zdarzeń w magazynie zwanym **Event Store**.
- Zdarzenia są źródłem prawdy, a stan jest pochodną.

<div class="columns">
<div>

**Zyski**
- pełny audyt i historia
- stan na dowolny moment
- nowe widoki z całej historii (replay)

</div>
<div>

**Koszty**
- wersjonowanie zdarzeń (upcasting)
- brak „poprawiania” historii
- obowiązkowe modele odczytu
- RODO a niezmienny log

</div>
</div>

---

# <span class="branch">19-event-sourcing</span> Agregat ze zdarzeń

<div class="columns">
<div>

```java
public void registerTransaction(Transaction t) {
  validate(t);
  record(new TransactionRegistered(number, t));
}

private void apply(CardEvent event) {
  switch (event) {
    case CardIssued i -> { …; balance = zero(currency); }
    case TransactionRegistered r -> { balance = …; }
  }
  version++;
}
```

</div>
<div>

- `card_events`: append-only, PK `(stream_id, version)` = optimistic concurrency.
- Migawki co N zdarzeń (`card_snapshots`).
- Jawny format zapisu `CardIssued.v1`.
- Port `CardRepository` i use case'y **bez zmian**.
- Testy given (zdarzenia) / when (komenda) / then (zdarzenia).

</div>
</div>

**Ćwiczenie „blog”, część 4:** CQRS i projekcje dla bloga.

---

# Wzorzec Saga

- Proces biznesowy przechodzący przez wiele modułów lub usług z osobnymi transakcjami.
- Zamiast transakcji rozproszonej: **sekwencja lokalnych transakcji** + **kompensacje**.
- Kompensacja jest **semantyczna** (zwrot środków), a nie techniczny rollback.
- Kompensujemy ukończone kroki w **odwrotnej kolejności**.
- Wymagania praktyczne:
  - idempotentne kroki (at-least-once),
  - trwały stan procesu (recovery po restarcie),
  - timeouty i ponawianie,
  - plan na nieudaną kompensację.

<p class="small">blog.couchbase.com/saga-pattern-implement-business-transactions-using-microservices-part-2</p>

---

# <span class="branch">20-saga-orchestration</span> Orkiestracja

```
CHARGE_PAYMENT ──ok──► SCHEDULE_SHIPMENT ──ok──► CONFIRMED / COMPLETED
     │ rejected                 │ rejected
     ▼                          ▼
  REJECTED              REFUND_PAYMENT ──► CANCELLED / COMPENSATED
```

- `OrderSagaOrchestrator` wysyła polecenia i reaguje na odpowiedzi.
- `OrderSaga`: trwały stan, stos ukończonych kroków, liczba prób.
- Stan + polecenie w jednej transakcji (outbox).
- Idempotentność: `ProcessedMessages` w płatnościach, ignorowanie odpowiedzi, na które saga nie czeka.
- `SagaTimeoutScheduler`: ponowienie, a po limicie kompensacja.

---

# <span class="branch">21-saga-choreography</span> Choreografia

| | Orkiestracja | Choreografia |
|---|---|---|
| proces | jedna klasa | rozproszony po listenerach |
| stan | jawny (`order_sagas`) | niejawny (statusy + lokalne kopie) |
| komunikaty | polecenia + odpowiedzi | fakty |
| zależności | orkiestrator zna uczestników | wspólny moduł `events` |
| timeouty, monitoring | wbudowane | trzeba dobudować |

- Bez modułu `events` powstałby cykl `orders → payments → shipping → orders`.
- Kolejność zdarzeń nie jest gwarantowana: ponawianie nieudanych publikacji.

**Ćwiczenie końcowe:** rozproszona saga provisioningu infrastruktury (5 kroków, 2 serwisy, AC1–AC6).

---

# Podsumowanie

1. Zacznij od **modelu domeny** i reguły zależności. Framework to szczegół.
2. Porty i adaptery dają **wymienność** i **szybkie testy**.
3. Granice pilnuj **automatycznie** (ArchUnit, Modulith, build).
4. **Modularny monolit** to dobry punkt docelowy. Mikroserwis to wymiana adapterów, a nie przepisanie domeny.
5. CQRS, Event Sourcing i saga rozwiązują konkretne problemy, ale **mają swoją cenę**.
6. Każda decyzja to kompromis. Zapisuj ją w **ADR**.

---

# Literatura

- R. C. Martin, *Clean Architecture*
- T. Hombergs, *Get Your Hands Dirty on Clean Architecture*: reflectoring.io/book
- O. Drotbohm, *Spring Modulith deep dive*: github.com/odrotbohm/spring-modulith-deep-dive
- *Modular Architecture with Spring*: leanpub.com/maws
- N. Ford, M. Richards et al., *Software Architecture: The Hard Parts*
- M. Richards, N. Ford, *Fundamentals of Software Architecture*
- Proxy i AOP: refactoring.guru/design-patterns/proxy, docs.spring.io/spring-framework/reference/core/aop.html
- Model C4: c4model.com
