# 4. Osobny model persystencji i optimistic locking

- Status: zaakceptowana
- Branch: `07-persistence-adapter`

## Kontekst
Agregat `Card` nie ma setterów, konstruktora bezargumentowego ani adnotacji JPA. Dopasowanie go do JPA
wymagałoby osłabienia enkapsulacji i wprowadzenia zależności od `jakarta.persistence` do domeny.

## Decyzja
- Adapter `persistence` ma własny model (`CardEntity`, `TransactionEntity`) i mapper dwukierunkowy (`JpaCardMapper`).
- Agregat odtwarzamy przez `Card.restore()`.
- Współbieżne modyfikacje wykrywamy przez `@Version`. Adapter tłumaczy błąd na `CardModifiedConcurrentlyException`, czyli wyjątek zdefiniowany przez port. Use case ponawia operację (`@Retry`).
- Transakcję otwiera use case (`@Atomic`), a adapter wymaga jej istnienia (`Propagation.MANDATORY`).

## Konsekwencje
- Domena jest wolna od JPA, a schemat bazy może ewoluować niezależnie.
- Kod mapowania trzeba napisać i przetestować (`JpaCardRepositoryAdapterTest`).
- Ładowanie całego agregatu z transakcjami nie skaluje się przy dużej liczbie transakcji. Wrócimy do tego w CQRS/ES.
