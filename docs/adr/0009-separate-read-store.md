# 9. Osobny magazyn modelu odczytu (MongoDB) zasilany zdarzeniami

- Status: zaakceptowana
- Branch: `18-cqrs-read-store`

## Kontekst
W `17` strona odczytu czytała tabele strony zapisu. To proste, ale wiąże schemat zapisu z zapytaniami.
Obie strony konkurują o tę samą bazę i nie da się ich skalować niezależnie.

## Decyzja
- Model odczytu (`card_summaries`) w MongoDB to zdenormalizowany dokument na kartę.
- Zasilają go zdarzenia aplikacyjne (`CardAdded`, `TransactionAdded`) przez `@ApplicationModuleListener`, z gwarancją dostarczenia z Event Publication Registry.
- Projekcja jest idempotentna (deduplikacja po `transactionId`) i odporna na kolejność (upsert).
- Model odczytu można w każdej chwili odbudować ze źródła prawdy (`CardProjectionRebuilder`).

## Konsekwencje
- Odczyty nie obciążają bazy zapisu, a kształt widoku zmienia się bez migracji tabel.
- Spójność ostateczna: tuż po komendzie odczyt może zwrócić stare dane.
- Druga technologia do utrzymania. Testy strony odczytu wymagają MongoDB (Testcontainers).
