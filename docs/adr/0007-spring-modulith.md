# 7. Modularny monolit ze Spring Modulith

- Status: zaakceptowana (zastępuje część decyzji z `12` i `13`)
- Branch: `15-spring-modulith`

## Kontekst
Moduły Maven i JPMS skutecznie pilnują granic, ale kosztują: wiele `pom.xml`, `module-info`, testowe aplikacje
per moduł, walkę z refleksją. Zdarzenia między modułami były trzymane w pamięci i mogły ginąć.

## Decyzja
- Jeden artefakt Maven. Moduły aplikacyjne to pakiety najwyższego poziomu (`payments`, `orders`, `common`).
- Granice weryfikuje `ApplicationModules.verify()`: API modułu to pakiet bazowy i `@NamedInterface` (`payments::api`), `allowedDependencies` zawęża zależności `orders`.
- Komunikacja asynchroniczna przez `@ApplicationModuleListener` z Event Publication Registry (JPA). Stan modułu i zdarzenie zapisują się w jednej transakcji (transactional outbox).
- Odrzucenie płatności nie wycofuje transakcji listenera (`@Atomic(noRollbackFor = …)`).

## Konsekwencje
- Granice sprawdza test, a nie kompilator (jak w `11`), ale z gotowymi regułami i dokumentacją (`Documenter`).
- Dostarczanie zdarzeń jest at-least-once, więc listenery muszą być idempotentne.
- Moduły można testować w izolacji (`@ApplicationModuleTest` + `Scenario`).
- Zamówienia mają własną tabelę (`orders`). Każdy moduł jest właścicielem swoich danych.
