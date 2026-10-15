# 05 – Spring jako szczegół

> Moduł 2: Implementacja · poprzedni: `04-cross-cutting-by-hand` · następny: `06-rest-adapter`

## Cel
Dodać Spring Boot **bez zmiany domeny i use case'ów**. Spring przejmuje rolę composition root (IoC/DI)
oraz ręcznych proxy z `04` (AOP). Rdzeń aplikacji nadal nie wie o jego istnieniu.

## Problem z poprzedniego kroku
Ręczne składanie obiektów (`PaymentsFacade`, `PaymentsFactory`) i własne proxy dobrze uczą mechaniki,
ale w realnej aplikacji to kod, którego nie chcemy utrzymywać.

## Co się zmieniło
```bash
git diff 04-cross-cutting-by-hand..05-spring-as-detail --stat
```
| Było w 04 | Jest w 05 |
|---|---|
| `PaymentsFacade` + `PaymentsFactory` | `PaymentsConfiguration` (`@Bean` dla use case'ów) + `@Adapter` (component scan) |
| `Proxies` + `*Interceptor` | `common/aop/*Aspect` (Spring AOP, AspectJ annotations) |
| `ConsoleTransactionManager` | `PlatformTransactionManager` Springa (na razie JDBC/H2, JPA w `07`) |
| `Application.main` z wywołaniami | `@SpringBootApplication` + `DemoRunner` (adapter CLI, profil `demo`) |

Bez zmian: `domain`, `application` (poza deklaratywnymi `@Atomic`/`@Timer` z `04`).

## Na co zwrócić uwagę
- `ArchitectureTest.domain_and_application_do_not_depend_on_frameworks` pilnuje, że rdzeń nie importuje Springa ani JPA.
- `AspectOrder`: retry → timer → atomic → metoda. Retry musi obejmować transakcję, żeby każda próba miała nową (`AspectsTest.failed_attempts_are_retried`).
- `AspectsTest` czyta stan beana przez metody, bo bean jest proxy CGLIB i jego pola są puste. To dobra okazja, żeby pokazać, jak działa proxy klasowe.
- Testy domeny i use case'ów z `01`/`02` przechodzą bez zmian i bez Springa.
- ADR: `docs/adr/0003-spring-as-implementation-detail.md`.

## Kiedy NIE stosować
Własne adnotacje zamiast `@Transactional` mają sens, gdy naprawdę chcemy rdzenia niezależnego od frameworka.
Wiele zespołów świadomie akceptuje `@Transactional` w warstwie aplikacji, bo jest to pragmatyczne i dobrze znane.

## Pytania do dyskusji
- Co dokładnie zyskujemy, trzymając Springa poza `application`? Ile to kosztuje?
- Dlaczego adnotacja `@Atomic` na metodzie wywołanej przez `this` nie zadziała?
- Proxy JDK czy CGLIB: kiedy Spring wybiera które?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```
