# 2. Logika biznesowa niezależna od infrastruktury (porty i adaptery)

- Status: zaakceptowana
- Branch: `02-use-cases-and-ports`

## Kontekst
W `00-layered-anemic` logika zależała od JPA, zegara systemowego i Springa. Testy były wolne, a zmiana technologii dotykała reguł biznesowych.

## Decyzja
- Kod dzielimy na `domain` (model, reguły), `application` (use case'y i porty) oraz `adapters` (technologia).
- Zależności wskazują wyłącznie do środka: `adapters → application → domain` (Dependency Inversion Principle).
- Warstwa aplikacji definiuje **porty wejściowe** (interfejsy use case'ów + komendy) i **porty wyjściowe** (`CardRepository`, `DateTimeProvider`, …).
- Wszystkie implementacje składa jedno miejsce, czyli composition root (`Application.main`).

## Konsekwencje
- Domenę i use case'y testujemy bez frameworka, na fake'ach portów.
- Więcej typów (porty, komendy, modele wyjściowe) i mapowanie na granicach.
- Framework (Spring) dodamy jako szczegół implementacyjny w `05-spring-as-detail`.
