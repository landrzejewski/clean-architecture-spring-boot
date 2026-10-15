# 04 – Zagadnienia przekrojowe „ręcznie”: Decorator, Proxy, Factory, Facade

> Moduł 2: Implementacja · poprzedni: `03-architectures-compared` · następny: `05-spring-as-detail`

## Cel
Pokazać, jak oddzielić zagadnienia przekrojowe (logowanie, pomiar czasu, transakcje) od logiki biznesowej
**bez frameworka**, używając klasycznych wzorców GoF. W `05` zobaczymy, że Spring AOP robi dokładnie to samo.

## Problem z poprzedniego kroku
Use case'y potrzebują transakcji i logowania. Wpisanie tego do `AddTransactionService` zmieszałoby logikę
aplikacji z infrastrukturą i powieliło ten sam kod w każdym use case.

## Co się zmieniło
```bash
git diff 03-architectures-compared..04-cross-cutting-by-hand --stat
```

| Wzorzec | Klasa | Rola |
|---|---|---|
| Decorator | `adapters/decorators/LoggingAddTransactionUseCase` | statyczny dekorator jednego interfejsu |
| Proxy (JDK dynamic proxy) | `common/proxy/Proxies` | generyczny proxy dla dowolnego interfejsu |
| Chain of Responsibility | `Proxies.Invocation.proceed()` | łańcuch interceptorów w jednym proxy |
| Interceptory | `LoggingInterceptor`, `TimerInterceptor`, `AtomicInterceptor` | aspekty sterowane adnotacjami `@Atomic`, `@Timer` |
| Abstract Factory | `PaymentsFactory`, `DevPaymentsFactory`, `TestPaymentsFactory` (test) | spójne rodziny implementacji portów |
| Facade | `PaymentsFacade` | jeden punkt wejścia do modułu, ukrywa składanie obiektów |

## Na co zwrócić uwagę
- Adnotacje `@Atomic` i `@Timer` deklarują *intencję* i nie zależą od żadnej technologii. Warstwa aplikacji nadal nie zna infrastruktury.
- JDK proxy działa tylko dla interfejsów. To praktyczny argument za portami wejściowymi.
- Zagnieżdżanie proxy w proxy ukrywa adnotacje celu (interceptor widzi `$Proxy0`). Dlatego `Proxies` buduje **łańcuch interceptorów w jednym proxy**, tak jak Spring AOP.
- Kolejność interceptorów ma znaczenie. Czy pomiar czasu ma obejmować commit transakcji?
- Zdarzenie `TransactionAdded` jest publikowane **przed commitem**. Co się stanie, gdy commit się nie powiedzie? Wrócimy do tego w `15` (outbox).
- `AtomicInterceptorTest` pokazuje commit, rollback i ponowne rzucenie oryginalnego wyjątku.

## Kiedy NIE stosować
Ręczne proxy to materiał dydaktyczny. W aplikacji Spring używamy AOP (`05`). Własne interceptory mają sens
w bibliotekach bez frameworka albo tam, gdzie liczy się kontrola nad kolejnością i kosztem.

## Pytania do dyskusji
- Jaka jest różnica między Decorator a Proxy, skoro implementacja wygląda podobnie?
- Co tracimy, gdy logika transakcyjna jest „niewidoczna” w kodzie use case'u?
- Dlaczego wywołanie metody `this.inna()` wewnątrz obiektu omija proxy? (self-invocation)

## Uruchomienie
```bash
./mvnw test
./mvnw -q compile && java -cp target/classes pl.fullstackdeveloper.Application
```
