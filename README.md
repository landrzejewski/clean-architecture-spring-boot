# Czysta architektura w praktyce (Spring Boot)

Materiały szkoleniowe: od aplikacji warstwowej przez czystą architekturę, modularny monolit i mikroserwis
do CQRS, Event Sourcingu i sag. Każdy krok to **osobny branch** zbudowany na poprzednim, na jednej domenie
(karty i płatności, potem zamówienia i wysyłki).

- Prezentacja: [`presentation/slides.html`](presentation/slides.html). Źródło Marp: [`presentation/slides.md`](presentation/slides.md).
- Materiał źródłowy: `Clean architecture.pdf`.

## Jak pracować z repozytorium
```bash
git checkout 06-rest-adapter                              # stan po kroku
git diff 05-spring-as-detail..06-rest-adapter --stat      # co wniósł krok
./mvnw test                                               # każda reguła ma test
```
Na każdym branchu `README.md` opisuje cel, problem z poprzedniego kroku, zmiany, kiedy **nie** stosować
rozwiązania, pytania do dyskusji i (na końcu modułu) ćwiczenie „blog”. Decyzje są w `docs/adr`, model C4 w `docs/c4/workspace.dsl`.

```
00 → 01 → 02 → … → 14 → 15-spring-modulith ─┬─► 16-extract-microservice
                                             ├─► 17-cqrs-light → 18-cqrs-read-store → 19-event-sourcing
                                             └─► 20-saga-orchestration → 21-saga-choreography
```

## Agenda i branche

### Moduł 1: Wprowadzenie do czystej architektury
| Punkt agendy | Branch |
|---|---|
| Problemy źle dobranej architektury, architektura warstwowa, model anemiczny | `00-layered-anemic` |
| Spójność i sprzężenie, bogaty model domeny (agregat, Value Object, zdarzenia), SRP | `01-rich-domain` |
| Kontrakty, IoC/DI, DIP, porty i adaptery, composition root | `02-use-cases-and-ports` |
| Przegląd architektur: warstwowa, onion, heksagonalna, czysta | `03-architectures-compared` |

### Moduł 2: Implementacja czystej architektury
| Punkt agendy | Branch |
|---|---|
| Zagadnienia przekrojowe: Decorator, Proxy, Factory, Facade | `04-cross-cutting-by-hand` |
| Separacja logiki od infrastruktury, Spring jako szczegół, AOP | `05-spring-as-detail` |
| Adapter REST, kontrakt API, walidacja, Problem Details | `06-rest-adapter` |
| Adapter persystencji, mapowanie modeli między warstwami | `07-persistence-adapter` |
| Wymienność adapterów, Anti-Corruption Layer (API NBP), drugi kanał wejściowy | `08-adapter-swap` |
| Testowanie, architektura jako kod (ArchUnit), Testcontainers | `09-testing-architecture` |

### Moduł 3: Modularyzacja aplikacji
| Punkt agendy | Branch |
|---|---|
| Drugi bounded context i skutki braku granic | `10-second-context-naive` |
| Ograniczanie dostępu na poziomie języka (`api`, package-private, `sealed`) | `11-language-encapsulation` |
| Moduły Maven | `12-maven-modules` |
| Java modules (JPMS) | `13-jpms` |
| Komunikacja modułów: własny port, ACL, sync i async | `14-module-communication` |
| Spring Modulith, transactional outbox, testy modułów, dokumentacja z kodu | `15-spring-modulith` |
| Migracja do mikroserwisów: HTTP, Kafka, kontrakty | `16-extract-microservice` |

### Moduł 4: Wzorce zaawansowane (rozszerzenie agendy)
| Punkt agendy | Branch |
|---|---|
| CQS/CQRS, szyna komend i zapytań, model odczytu | `17-cqrs-light` |
| CQRS z osobnym magazynem odczytu (MongoDB), projekcje, spójność ostateczna | `18-cqrs-read-store` |
| Event Sourcing, event store, migawki, replay | `19-event-sourcing` |
| Saga: orkiestracja, kompensacje, idempotentność, timeouty | `20-saga-orchestration` |
| Saga: choreografia i porównanie z orkiestracją | `21-saga-choreography` |

## Ćwiczenie „blog”
| Część | Po branchu | Zakres |
|---|---|---|
| 1 | `03` | domena i warstwa aplikacji bloga w czystej Javie |
| 2 | `09` | adaptery REST i JPA, testy, reguły ArchUnit |
| 3 | `16` | drugi kontekst (notifications) jako moduł Modulith, opcjonalnie osobna usługa |
| 4 | `19` | CQRS i projekcje, opcjonalnie Event Sourcing artykułu |
| końcowe | `21` | rozproszona saga „provisioning infrastruktury” |

## Wymagania
- JDK 25 (Spring Boot 4.0, Spring Modulith 2.1), Maven Wrapper w repozytorium.
- Docker do testów z Testcontainers (PostgreSQL od `09`, Kafka w `16`, MongoDB w `18`–`19`). Bez Dockera te testy są pomijane.
- Podgląd modelu C4: `docker run -it --rm -p 8080:8080 -v $(pwd)/docs/c4:/usr/local/structurizr structurizr/lite`

## Prezentacja
```bash
cd presentation
npx @marp-team/marp-cli@4.5.1 --theme-set theme.css --html -o slides.html -- slides.md
```
