# 02 – Use case'y, porty i adaptery

> Moduł 1: Dlaczego architektura · poprzedni: `01-rich-domain` · następny: `03-architectures-compared`

## Cel
Dodać warstwę aplikacji wokół domeny: use case'y z kontraktami (porty wejściowe), zależności wyrażone
jako interfejsy (porty wyjściowe) i pierwsze adaptery. Całość składamy ręcznie, nadal bez frameworka.

## Problem z poprzedniego kroku
Model z `01` jest poprawny, ale nikt go nie wywołuje. Potrzebujemy miejsca, które pobierze kartę, zarejestruje
transakcję, zapisze zmiany i powiadomi resztę systemu, i to bez wiedzy o bazie, zegarze czy kolejce.

## Co się zmieniło
```bash
git diff 01-rich-domain..02-use-cases-and-ports --stat
```

| Rola | Typy | Pakiet |
|---|---|---|
| Porty wejściowe (driving) | `AddCardUseCase`, `AddTransactionUseCase`, `GetCardUseCase`, `GetCardsUseCase` | `application` |
| Komendy (self-validating) | `AddCardCommand`, `AddTransactionCommand` | `application` |
| Implementacje use case'ów | `*Service` | `application` |
| Model wyjściowy | `CardView` | `application` |
| Porty wyjściowe (driven) | `CardRepository`, `DateTimeProvider`, `CardNumberGenerator`, `TransactionEventPublisher` | `application` |
| Adaptery | `HashMapCardRepository`, `SystemDateTimeProvider`, `RandomCardNumberGenerator`, `ConsoleTransactionEventPublisher` | `adapters` |
| Composition root | `Application.main` | root |

Kierunek zależności: `adapters → application → domain`. Domena nie zna nikogo.

## Na co zwrócić uwagę
- `AddTransactionService` nie ma ani jednej reguły biznesowej, bo tylko orkiestruje. Reguły są w `Card`.
- Zdarzenie domenowe `TransactionRegistered` zostaje w module. Na zewnątrz idzie zdarzenie aplikacyjne `TransactionAdded` z typami prostymi.
- `AddTransactionServiceTest` używa fake'ów portów (`InMemoryCardRepository`, lambda jako `DateTimeProvider`), więc czas jest w pełni kontrolowany.
- Nie ma frameworka, a mimo to mamy wstrzykiwanie zależności (IoC/DI przez konstruktor).
- Dokumentacja: `docs/adr` (decyzje) i `docs/c4/workspace.dsl` (model C4: Context, Container, Component).

## Kiedy NIE stosować
- Interfejs dla use case'u, który ma jedną implementację, to koszt. Wiele zespołów pomija porty wejściowe i wstrzykuje klasę use case'u. To świadomy kompromis.
- Proste CRUD-y nie potrzebują portów dla każdej operacji.

## Pytania do dyskusji
- Kto jest właścicielem interfejsu `CardRepository`: warstwa aplikacji czy adapter? Dlaczego to ważne (DIP)?
- Czy `CardView` to nadmiarowe mapowanie? Co by się stało, gdyby use case zwracał `Card`?
- Gdzie walidować dane wejściowe: w kontrolerze, w komendzie czy w domenie?

## Uruchomienie
```bash
./mvnw test
./mvnw -q compile && java -cp target/classes pl.fullstackdeveloper.Application
```
