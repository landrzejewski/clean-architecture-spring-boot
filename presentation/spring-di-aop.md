---
marp: true
theme: theory
paginate: true
lang: pl
title: "Dependency Injection i AOP w Springu"
style: |
  .cols-code { display: grid; grid-template-columns: 1.55fr 1fr; gap: 30px; align-items: start; }
  .cols-narrow { display: grid; grid-template-columns: 1fr 1.6fr; gap: 30px; align-items: start; }
  section :not(pre) > code { white-space: nowrap; }
---

<!-- _class: title -->
<!-- _paginate: false -->

# Dependency Injection<br>i AOP w Springu

---

# Kto tworzy zależności?

Serwis dodający transakcję potrzebuje repozytorium kart i źródła aktualnego czasu. Może utworzyć je sam albo dostać gotowe z zewnątrz.

<div class="cols">
<div class="card bad">

## Klasa sama tworzy zależności

```java
class AddTransactionService {
  private final CardRepository cards =
      new HashMapCardRepository();
  private final DateTimeProvider time =
      new SystemDateTimeProvider();
}
```

Implementacja jest wpisana na stałe. Przejście na bazę danych wymaga zmiany serwisu, a test nie może podstawić własnego repozytorium ani ustalonej daty.

</div>
<div class="card good">

## Zależności przekazane przez konstruktor

```java
class AddTransactionService {
  private final CardRepository cards;
  private final DateTimeProvider time;

  AddTransactionService(CardRepository cards,
                        DateTimeProvider time) {
    this.cards = cards;
    this.time = time;
  }
}
```

Serwis zna tylko interfejsy. O tym, jaka implementacja trafi do środka, decyduje kod, który składa aplikację.

</div>
</div>

<div class="takeaway">

**Inversion of Control** to zasada: obiekt nie decyduje sam o tworzeniu swoich zależności. **Dependency Injection** to technika, która ją realizuje. W Springu obiekty tworzy i łączy kontener.

</div>

---

# Kontener Springa: beany i ApplicationContext

Kontenerem IoC w Springu jest `ApplicationContext`. Obiekty, którymi zarządza, nazywamy **beanami**. Kontener czyta konfigurację, tworzy na jej podstawie definicje beanów, a potem buduje z nich graf obiektów.

<div class="chain">
<div class="box">konfiguracja<span class="sub"><code>@Component</code>, <code>@Bean</code>, <code>application.yml</code></span></div><div class="arr">→</div>
<div class="box soft">definicje beanów<span class="sub">klasa, zależności, zasięg</span></div><div class="arr">→</div>
<div class="box core">ApplicationContext<span class="sub">tworzy i wstrzykuje</span></div><div class="arr">→</div>
<div class="box good">graf obiektów<span class="sub">gotowy do użycia</span></div>
</div>

<div class="cols">
<div>

**Co robi kontener:**
- tworzy obiekty we właściwej kolejności: najpierw zależności, potem obiekty, które z nich korzystają,
- dopasowuje zależności po typie, a gdy to nie wystarcza, po nazwie lub kwalifikatorze,
- zarządza cyklem życia: wywołuje metody inicjalizujące i zamykające,
- może opakować bean w **proxy**, które dodaje zachowanie, np. transakcję. Na tym opiera się AOP.

</div>
<div>

```java
var context = SpringApplication.run(Application.class, args);
var useCase = context.getBean(AddTransactionUseCase.class);
```

Kod aplikacji prawie nigdy nie pobiera beanów z kontekstu ręcznie. Wystarczy zadeklarować zależność w konstruktorze, a kontener ją dostarczy.

</div>
</div>

<div class="takeaway">

Bean to zwykły obiekt Javy. Różni się od innych obiektów tylko tym, że tworzy go i łączy z innymi kontener.

</div>

---

# Rejestracja beanów: skanowanie lub konfiguracja

Kontener musi wiedzieć, jakie obiekty ma utworzyć. Są dwa podstawowe sposoby: oznaczyć klasę adnotacją albo zadeklarować bean w klasie konfiguracyjnej.

<div class="cols">
<div class="card">

## Skanowanie komponentów

```java
@Component
class HashMapCardRepository
    implements CardRepository { ... }
```

Spring przeszukuje pakiet klasy startowej i jego podpakiety, a każdą klasę z `@Component` rejestruje jako bean. Odmiany tej adnotacji, czyli `@Service`, `@Repository` i `@RestController`, działają tak samo i lepiej opisują rolę klasy. Można też tworzyć własne stereotypy, np. `@Adapter`.

</div>
<div class="card good">

## Klasa konfiguracyjna z metodami @Bean

```java
@Configuration
class PaymentsConfiguration {

  @Bean
  GetCardUseCase getCardUseCase(CardRepository cards) {
    return new GetCardService(cards);
  }
}
```

Metoda tworzy obiekt jawnie, a jej parametry to zależności, które dostarczy kontener. Klasa `GetCardService` nie potrzebuje żadnej adnotacji Springa.

</div>
</div>

<div class="takeaway">

Skanowanie jest wygodne dla adapterów, które i tak zależą od frameworka. `@Bean` sprawdza się dla klas z bibliotek oraz dla logiki aplikacji, którą chcemy utrzymać bez zależności od Springa.

</div>

---

# Wstrzykiwanie przez konstruktor

Spring potrafi wstrzyknąć zależność przez konstruktor, przez setter albo bezpośrednio do pola. Zalecany jest konstruktor, bo obiekt jest kompletny od chwili utworzenia.

<div class="cols">
<div>

```java
@Service
class GetCardService implements GetCardUseCase {
  private final CardRepository cards;
  GetCardService(CardRepository cards) {
    this.cards = cards;
  }
}
```

- Jeśli klasa ma jeden konstruktor, Spring używa go automatycznie i adnotacja `@Autowired` nie jest potrzebna.
- Pola mogą być `final`, więc zależności nie da się podmienić ani zapomnieć ich ustawić.
- Długa lista parametrów od razu pokazuje, że klasa ma za dużo odpowiedzialności.

</div>
<div>

**Wstrzykiwanie do pola** (`@Autowired private CardRepository cards`) ukrywa zależności, wyklucza `final` i sprawia, że obiektu nie da się utworzyć bez Springa.

**Konstruktor pozwala testować bez kontenera.** Test jednostkowy tworzy obiekt jak każdy inny i przekazuje mu prostą implementację portu:

```java
var cards = new HashMapCardRepository();
var service = new GetCardService(cards);
```

Test z kontenerem (`@SpringBootTest`) jest potrzebny dopiero wtedy, gdy chcemy sprawdzić, czy cała aplikacja poprawnie się składa.

</div>
</div>

<div class="takeaway">

Setter stosuje się wyjątkowo, dla zależności opcjonalnych. W kodzie aplikacji domyślnym wyborem jest konstruktor.

</div>

---

# Kilka implementacji jednego interfejsu

Kontener dopasowuje zależności po typie. Jeśli dla interfejsu istnieją dwa beany, aplikacja nie wystartuje i zgłosi `NoUniqueBeanDefinitionException`, bo kontener nie wie, który z nich wybrać.

<div class="cols">
<div>

```java
interface ExchangeRateProvider {
  ExchangeRate getRate(Currency from, Currency to);
}

@Component
@Primary
class NbpExchangeRateAdapter
    implements ExchangeRateProvider { ... }

@Component("fixedRates")
class FixedExchangeRateAdapter
    implements ExchangeRateProvider { ... }
```

</div>
<div>

```java
// trafi bean oznaczony @Primary
CurrencyExchange(ExchangeRateProvider provider)
// konkretny bean wskazany nazwą
CurrencyExchange(@Qualifier("fixedRates")
                 ExchangeRateProvider provider)
// wszystkie implementacje naraz
CurrencyExchange(List<ExchangeRateProvider> all)
```

- `@Primary` wskazuje implementację domyślną.
- `@Qualifier` wybiera konkretny bean w miejscu wstrzyknięcia.
- `List<T>` przydaje się, gdy wszystkie implementacje mają działać razem, np. jako łańcuch reguł.

</div>
</div>

<div class="takeaway">

Gdy wybór implementacji zależy od środowiska, a nie od miejsca użycia, lepiej przenieść go do konfiguracji.

</div>

---

# Konfiguracja zewnętrzna: właściwości i warunki

Ten sam kod powinien działać w różnych środowiskach. Wartości i wybór implementacji przenosimy więc do `application.yml`, zmiennych środowiskowych lub argumentów uruchomienia.

<div class="cols">
<div>

```yaml
payments:
  card-number-length: 16
  exchange-rates:
    provider: fixed      # fixed | nbp
```

```java
@ConfigurationProperties("payments")
record PaymentsProperties(int cardNumberLength) { }
```

Rekord rejestruje się przez `@ConfigurationPropertiesScan`. Spring dopasowuje właściwości do pól po nazwie, a błędna wartość zatrzymuje start aplikacji. Pojedynczą wartość można też wstrzyknąć przez `@Value("${...}")`.

</div>
<div>

```java
@Adapter
@ConditionalOnProperty(
    name = "payments.exchange-rates.provider",
    havingValue = "nbp")
class NbpExchangeRateAdapter
    implements ExchangeRateProvider { ... }
```

- `@ConditionalOnProperty` tworzy bean tylko wtedy, gdy właściwość ma podaną wartość. Adapter stałych kursów ma `havingValue = "fixed"` i `matchIfMissing = true`, więc działa domyślnie.
- `@Profile("postgres")` włącza beany tylko przy aktywnym profilu, wybranym przez `spring.profiles.active`.

</div>
</div>

<div class="takeaway">

Zmiana adaptera to wtedy zmiana jednej linii konfiguracji, bez ingerencji w kod logiki.

</div>

---

# Zasięg i cykl życia beana

Zasięg (scope) określa, ile instancji beana tworzy kontener i jak długo one żyją. Domyślnym zasięgiem jest singleton, czyli jedna instancja na cały kontekst aplikacji.

| Zasięg | Liczba instancji | Typowe zastosowanie |
|---|---|---|
| `singleton` (domyślny) | jedna na kontekst | use case'y, serwisy, adaptery, czyli obiekty bezstanowe |
| `prototype` | nowa przy każdym pobraniu z kontenera | obiekty ze stanem, np. budowniczy raportu |
| `request`, `session` | jedna na żądanie HTTP lub sesję | dane bieżącego użytkownika w aplikacji webowej |

<div class="cols">
<div>

- Singleton jest współdzielony przez wszystkie wątki. Nie może trzymać w polach danych konkretnego wywołania, np. bieżącej karty czy kwoty.
- Bean `prototype` wstrzyknięty do singletona powstaje tylko raz, razem z singletonem. Po kolejne instancje trzeba sięgać przez `ObjectProvider<T>` i metodę `getObject()`.

</div>
<div>

```java
@Component
class ExchangeRateCache {
  @PostConstruct   // po wstrzyknięciu zależności
  void warmUp() { ... }
  @PreDestroy      // przy zamykaniu kontekstu
  void flush() { ... }
}
```

</div>
</div>

<div class="takeaway">

W typowej aplikacji prawie wszystkie beany są bezstanowymi singletonami. Stan biznesowy przechowują obiekty domeny i baza danych, a nie pola serwisów.

</div>

---

# Spring Boot: start i auto-konfiguracja

Spring Boot nie zastępuje kontenera, tylko go konfiguruje. Na podstawie bibliotek obecnych na classpath i właściwości tworzy typowe beany, np. `DataSource`, menedżer transakcji, `RestClient.Builder` czy serwer HTTP.

<div class="cols">
<div>

```java
@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
```

Adnotacja `@SpringBootApplication` łączy trzy inne:
- `@SpringBootConfiguration`, czyli odmianę `@Configuration`: klasa może deklarować beany,
- `@ComponentScan`: skanowanie pakietu klasy startowej i jego podpakietów,
- `@EnableAutoConfiguration`: włączenie auto-konfiguracji.

</div>
<div>

```java
// uproszczony fragment auto-konfiguracji
@AutoConfiguration
@ConditionalOnClass(DataSource.class)
class DataSourceAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  DataSource dataSource(DataSourceProperties p) {
    ...
  }
}
```

Auto-konfiguracja działa warunkowo. Jeśli zdefiniujemy własny bean danego typu, `@ConditionalOnMissingBean` sprawi, że Boot nie utworzy swojego.

</div>
</div>

<div class="takeaway">

Uruchomienie z flagą `--debug` wypisuje raport warunków: które auto-konfiguracje zadziałały i dlaczego. Od niego warto zacząć, gdy bean nie powstaje albo powstaje inny, niż oczekujemy.

</div>

---

# Cross-cutting concerns: kod, który powtarza się wszędzie

Transakcje, pomiar czasu, ponawianie, logowanie i kontrola dostępu dotyczą wielu use case'ów, ale nie są logiką biznesową. Pisane ręcznie przesłaniają reguły i powtarzają się w każdej klasie.

<div class="cols">
<div class="card bad">

## Kod techniczny w use case'ie

```java
public void handle(AddTransactionCommand command) {
  var start = System.nanoTime();
  for (int attempt = 1; ; attempt++) {
    var tx = transactions.begin();
    try {
      var card = cards.findByNumber(command.cardNumber());
      card.registerTransaction(...);
      cards.save(card);
      tx.commit(); break;
    } catch (OptimisticLockException e) {
      tx.rollback();
      if (attempt == 3) throw e;
    }
  }
  log.info("took {} µs", (System.nanoTime() - start) / 1000);
}
```

</div>
<div class="card good">

## Sama logika i deklaracje

```java
@Retry(on = OptimisticLockException.class)
@Timer
@Atomic
public void handle(AddTransactionCommand command) {
  var card = cards.findByNumber(command.cardNumber());
  card.registerTransaction(...);
  cards.save(card);
}
```

- Metoda zawiera tylko logikę. Adnotacje mówią, *co* ma się stać wokół niej, a nie *jak*.
- Kod techniczny powstaje raz, w aspekcie, i działa wszędzie tam, gdzie pojawi się adnotacja.

</div>
</div>

<div class="takeaway">

**Programowanie aspektowe (AOP)** wydziela takie zadania do aspektów i dołącza je do metod bez zmieniania ich kodu.

</div>

---

# Pojęcia AOP

AOP ma własne słownictwo. Większość pojęć odpowiada na dwa pytania: *gdzie* dołączyć dodatkowe zachowanie i *co* ma ono robić.

| Pojęcie | Znaczenie | Przykład |
|---|---|---|
| **Aspect** | moduł z kodem technicznym, złożony z pointcutów i advice | `TimerAspect` |
| **Join point** | miejsce, do którego można dołączyć aspekt; w Springu zawsze wywołanie metody beana | wywołanie `handle(...)` |
| **Pointcut** | wyrażenie wybierające join pointy, czyli *gdzie* | metody z adnotacją `@Timer` |
| **Advice** | kod wykonywany w wybranych miejscach, czyli *co* i *kiedy* | pomiar czasu wokół metody |
| **Target** | oryginalny obiekt, którego metody są przechwytywane | `AddTransactionService` |
| **Proxy** | obiekt pośredniczący, który kontener wstrzykuje zamiast targetu | podklasa utworzona przez CGLIB |
| **Weaving** | dołączenie aspektów do kodu; Spring robi to przy starcie, tworząc proxy | uruchomienie kontekstu |

**Rodzaje advice:** `@Before` działa przed metodą, `@AfterReturning` po jej poprawnym zakończeniu, `@AfterThrowing` po wyjątku, a `@After` zawsze, jak blok `finally`. `@Around` otacza całe wywołanie i decyduje, czy i ile razy je wykonać, dlatego to on obsługuje transakcje i ponawianie.

---

# Własny aspekt w trzech krokach

Aspekt to zwykły bean oznaczony `@Aspect`. Spring AOP używa adnotacji z biblioteki AspectJ, ale aspekty dołącza przez proxy, bez modyfikowania bajtkodu.

<div class="cols-code">
<div>

```java
@Aspect
@Component
class TimerAspect {
  private final Logger log = LoggerFactory.getLogger(getClass());
  @Around("@annotation(pl.fullstackdeveloper.common.annotations.Timer)")
  Object measure(ProceedingJoinPoint call) throws Throwable {
    var start = System.nanoTime();
    try {
      return call.proceed();       // wykonanie oryginalnej metody
    } finally {
      var micros = (System.nanoTime() - start) / 1_000;
      log.info("{} took {} µs", call.getSignature(), micros);
    }
  }
}
```

</div>
<div>

**1. Zależność** `spring-boot-starter-aspectj` (do Boot 3: `spring-boot-starter-aop`). Boot sam włącza wtedy obsługę aspektów.

**2. Adnotacja**, która nazywa intencję:

```java
@Target(METHOD)
@Retention(RUNTIME)
public @interface Timer { }
```

**3. Aspekt** (obok): advice `@Around` otacza wywołanie metod z `@Timer`, a `proceed()` wykonuje oryginalną metodę. Bez tego wywołania metoda w ogóle by się nie wykonała.

</div>
</div>

<div class="takeaway">

Od teraz wystarczy dopisać `@Timer` do metody dowolnego beana. Kod use case'u nie wie, że ktoś mierzy czas jego działania.

</div>

---

# Wyrażenia pointcut

Pointcut decyduje, które metody zostaną przechwycone. Najczytelniejsze są pointcuty oparte na adnotacjach, bo miejsce działania aspektu widać wtedy bezpośrednio w kodzie.

| Wyrażenie | Co wybiera |
|---|---|
| `@annotation(…Timer)` | metody oznaczone adnotacją `@Timer` |
| `@within(…Atomic)` | wszystkie metody klas oznaczonych `@Atomic` |
| `execution(* pl..application.*Service.*(..))` | dowolne metody klas `*Service` z pakietów `application` |
| `within(pl.fullstackdeveloper.payments..*)` | metody wszystkich klas z pakietu i jego podpakietów |
| `bean(*UseCase)` | beany, których nazwa kończy się na `UseCase` (tylko Spring AOP) |
| `execution(public * *(..)) && !@annotation(…Timer)` | wyrażenia łączy się operatorami `&&`, `\|\|` i `!` |

<div class="cols-wide">
<div>

```java
@Pointcut("within(pl.fullstackdeveloper..application..*)")
void applicationLayer() { }

@Before("applicationLayer()")
void logCall(JoinPoint call) {
  log.info("calling {}", call.getSignature());
}
```

</div>
<div>

**Nazwany pointcut** (`@Pointcut`) nadaje wyrażeniu nazwę, której można używać w wielu advice.

Wyrażenia oparte na pakietach i nazwach łatwo przestają pasować po refaktoryzacji. Adnotacje są odporne na zmianę nazw.

</div>
</div>

---

# Jak to działa: proxy

Przy starcie kontenera Spring sprawdza, czy do metod beana pasuje jakiś pointcut. Jeśli tak, w miejsce oryginalnego obiektu rejestruje i wstrzykuje **proxy**, które przechwytuje wywołania.

<div class="cols">
<div>

<div class="stack">
<div class="dark">kontroler wywołuje <code>useCase.handle(...)</code></div>
<div class="down">↓</div>
<div>proxy: retry → pomiar czasu → transakcja</div>
<div class="down">↓</div>
<div class="hot">target: <code>AddTransactionService.handle(...)</code></div>
</div>

- **JDK dynamic proxy** implementuje te same interfejsy co target. **CGLIB** tworzy w locie podklasę targetu. Spring Boot domyślnie używa CGLIB.
- Przechwytywane są tylko wywołania, które przechodzą przez proxy. Klasa i metody nie mogą być `final`, metody `private` są pomijane, a obiekty tworzone przez `new` nie mają proxy.

</div>
<div class="card bad">

## Pułapka: wywołanie z wnętrza klasy

```java
@Service
class StatementService {

  public void generateAll() {
    for (var card : cards.findAll()) {
      generate(card);   // this.generate(), bez proxy
    }
  }

  @Transactional
  public void generate(Card card) { ... }
}
```

Wywołanie `this.generate()` trafia bezpośrednio do targetu, więc transakcja nie zostanie otwarta. Rozwiązaniem jest przeniesienie metody do osobnego beana albo umieszczenie adnotacji na metodzie wywoływanej z zewnątrz.

</div>
</div>

---

# Kolejność aspektów i gotowe aspekty Springa

Jeśli na jedną metodę działa kilka aspektów, ich kolejność zmienia wynik. Do najczęstszych zadań Spring dostarcza gotowe aspekty, więc własne pisze się rzadko.

<div class="cols-narrow">
<div>

<div class="stack">
<div class="dark">wywołujący</div>
<div class="down">↓</div>
<div><code>@Retry</code>, <code>@Order(10)</code></div>
<div><code>@Timer</code>, <code>@Order(20)</code></div>
<div><code>@Atomic</code>, <code>@Order(30)</code></div>
<div class="down">↓</div>
<div class="hot">metoda use case'u</div>
</div>

<p class="mt">Mniejsza wartość <code>@Order</code> oznacza aspekt bardziej zewnętrzny. Retry musi otaczać transakcję, żeby każda próba działała w nowej transakcji, a nie w tej, która została już wycofana.</p>

</div>
<div>

| Adnotacja | Co robi | Jak włączyć |
|---|---|---|
| `@Transactional` | transakcja wokół metody | Boot, gdy jest `DataSource` |
| `@Cacheable` | wynik z cache zamiast wywołania | `@EnableCaching` |
| `@Async` | wykonanie w innym wątku | `@EnableAsync` |
| `@Retryable` | ponowienie po wyjątku (Spring 7) | `@EnableResilientMethods` |
| `@PreAuthorize` | kontrola uprawnień | `@EnableMethodSecurity` |

- `@Transactional` domyślnie wycofuje transakcję tylko przy wyjątkach niekontrolowanych (`RuntimeException`, `Error`). Dla wyjątków kontrolowanych trzeba podać `rollbackFor`.
- Wbudowany `@Retryable` jest zawsze stosowany na zewnątrz `@Transactional` i `@Cacheable`.

</div>
</div>

---

# Dobre praktyki

DI i AOP upraszczają kod tylko wtedy, gdy stosuje się je z umiarem. Kilka zasad warto przyjąć od początku projektu.

<div class="cols">
<div class="card good">

## Dependency Injection

- Wstrzykuj zależności przez konstruktor, a pola oznaczaj jako `final`.
- Zależ od interfejsów (portów), a nie od konkretnych adapterów. Wybór implementacji przenieś do konfiguracji.
- Domenę i logikę aplikacji utrzymuj bez adnotacji Springa, a beany rejestruj w klasie `@Configuration`. Framework pozostaje wtedy szczegółem, który można wymienić.
- Logikę testuj bez kontenera. `@SpringBootTest` zostaw do sprawdzenia, czy całość się składa, a zależności podmieniaj w nim przez `@MockitoBean`.

</div>
<div class="card">

## AOP

- Aspektów używaj do zadań technicznych: transakcji, pomiarów, ponawiania, bezpieczeństwa. Reguła biznesowa ukryta w aspekcie jest niewidoczna dla osoby czytającej kod.
- Wybieraj metody adnotacjami, które nazywają intencję (`@Atomic`, `@Retry`), a nie szerokimi wyrażeniami opartymi na nazwach pakietów.
- Pamiętaj o ograniczeniach proxy: wywołania z wnętrza klasy, metody `final` i `private`, obiekty spoza kontenera.
- Gdy na jednej metodzie działa kilka aspektów, ustal ich kolejność jawnie.

</div>
</div>

<div class="takeaway">

Na gałęzi `05-spring-as-detail` te zasady są zastosowane w praktyce: use case'y są składane w `PaymentsConfiguration`, a `@Atomic`, `@Retry` i `@Timer` to własne adnotacje obsługiwane przez aspekty Springa.

</div>
