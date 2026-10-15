# 13 – Java Platform Module System (JPMS)

> Moduł 3: Modularyzacja · poprzedni: `12-maven-modules` · następny: `14-module-communication`

## Cel
Dodać drugą, silniejszą warstwę hermetyzacji: **moduł eksportuje tylko wybrane pakiety**, a pozostałe są
niewidoczne dla innych modułów, nawet jeśli klasy są `public`. Do tego jawne `requires` zamiast „wszystko, co jest na classpath”.

## Problem z poprzedniego kroku
Moduły Maven kontrolują *które artefakty* są na classpath, ale nie *które pakiety* z nich wolno używać.
`app` zależy od `payments`, więc mógł użyć dowolnej klasy płatności, np. agregatu `Card`.
Zależności przechodnie też „przeciekają”: wszystko, co ciągnie `common`, jest widoczne w `orders`.

## Co się zmieniło
```bash
git diff 12-maven-modules..13-jpms --stat
```
| Moduł | `exports` | Uwagi |
|---|---|---|
| `pl.fullstackdeveloper.common` | `common`, `common.annotations`, `common.web` | `common.aop` **nie** jest eksportowany, tylko `opens` (Spring znajduje aspekty refleksją) |
| `pl.fullstackdeveloper.payments.api` | `payments.api` | cały kontrakt |
| `pl.fullstackdeveloper.payments` | **nic** | `open module`: refleksja tak, kompilacja nie |
| `pl.fullstackdeveloper.orders` | **nic** | `requires payments.api`, a `payments` nie jest nawet czytelny |
| `pl.fullstackdeveloper.app` | **nic** | composition root |

- Test E2E z `app` (`OrdersIntegrationTest`) używał `AddCardUseCase` i `Money`, więc przestał się kompilować:
  `package pl.fullstackdeveloper.payments.application is not visible`.
  Zastąpił go `ShopEndToEndTest`, który jak każdy klient korzysta z REST.
- `maven-surefire-plugin` dostał `useModulePath=false`: testy działają na classpath.

## exports vs opens
| | Dostęp w czasie kompilacji | Głęboka refleksja (prywatne pola) |
|---|---|---|
| `exports p` | tak | nie |
| `opens p` | nie | tak |
| `open module` | – | wszystkie pakiety |

Spring, Hibernate i Jackson potrzebują refleksji, a nie dostępu w kompilacji. Dlatego moduły kontekstów są `open`
i nic nie eksportują.

## Na co zwrócić uwagę
- JPMS w aplikacji Spring Boot działa przede wszystkim **w czasie kompilacji**. Fat jar uruchamia się na classpath, a tam moduły nie obowiązują.
- Pełne uruchomienie na module path jest możliwe, ale wymaga dopięcia `opens`/`requires` dla wszystkich bibliotek. Koszt utrzymania jest wysoki.
- JPMS ma też mechanizm usług: `provides PaymentsApi with …` / `uses PaymentsApi` (ServiceLoader), czyli DI bez frameworka.
- Nazwy modułów bibliotek Springa to *automatic modules* (`Automatic-Module-Name` w manifeście, np. `spring.context`).

## Kiedy NIE stosować
Gdy zespół nie zna JPMS albo biblioteki źle wspierają module path. Podobny efekt (bez refleksyjnych niespodzianek)
daje kombinacja modułów Maven, ArchUnit i Spring Modulith (`15`).

## Pytania do dyskusji
- Czy warto płacić złożonością `module-info` za ochronę, którą daje też ArchUnit?
- Dlaczego test E2E przez REST jest lepszy niż test, który zna klasy use case'ów?

## Uruchomienie
```bash
./mvnw test
./mvnw -DskipTests package && java -jar app/target/app-1.0-SNAPSHOT.jar
```
