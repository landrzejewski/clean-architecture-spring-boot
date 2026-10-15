# 3. Spring jako szczegół implementacyjny

- Status: zaakceptowana
- Branch: `05-spring-as-detail`

## Kontekst
Potrzebujemy kontenera IoC, transakcji i AOP. Nie chcemy jednak, żeby domena i use case'y zależały od Springa.
Adnotacje frameworka w rdzeniu utrudniają testy i wymianę technologii.

## Decyzja
- `domain` i `application` nie importują `org.springframework..` ani `jakarta.persistence..`. Pilnuje tego `ArchitectureTest`.
- Use case'y rejestrujemy jako `@Bean` w `PaymentsConfiguration` (composition root modułu).
- Zagadnienia przekrojowe deklarujemy własnymi adnotacjami (`@Atomic`, `@Retry`, `@Timer`) z pakietu `common.annotations`. Aspekty w `common.aop` tłumaczą je na mechanizmy Springa.
- Adaptery oznaczamy stereotypem `@Adapter`, bo adaptery mogą znać framework.

## Konsekwencje
- Rdzeń da się uruchomić i przetestować bez Springa (testy z `01`/`02` działają bez zmian).
- Utrzymujemy własne aspekty zamiast `@Transactional`. Trzeba znać ich kolejność (`AspectOrder`).
- Działają ograniczenia proxy Springa: self-invocation, metody muszą być publiczne, klasy nie mogą być `final`.
