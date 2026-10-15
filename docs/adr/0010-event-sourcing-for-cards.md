# 10. Event Sourcing dla agregatu karty

- Status: zaakceptowana (zastępuje ADR 0004 w części dotyczącej utrwalania kart)
- Branch: `19-event-sourcing`

## Kontekst
Historia transakcji karty ma wartość biznesową (audyt, reklamacje, analizy). Model „stan w tabeli”
traci informację o tym, jak do stanu doszło. CQRS z `18` i tak opiera się na zdarzeniach.

## Decyzja
- Źródłem prawdy jest strumień zdarzeń karty w `card_events` (append-only, klucz `(stream_id, version)`).
- Agregat zmienia stan wyłącznie w `apply(CardEvent)`. Komendy rejestrują nowe zdarzenia, odczyt odtwarza stan z historii.
- Migawki (`card_snapshots`) co `payments.snapshot-frequency` zdarzeń skracają odtwarzanie.
- Zdarzenia zapisujemy w jawnym, wersjonowanym formacie (`CardIssued.v1`, `TransactionRegistered.v1`), a nie jako serializację klas domenowych.
- Modele odczytu buduje się przez replay historii (`PaymentsEventHistory`).

## Konsekwencje
- Pełny audyt, możliwość odtworzenia stanu na dowolny moment i budowy nowych widoków z przeszłości.
- Zdarzeń nie da się „poprawić”. Błędy naprawiamy kolejnymi zdarzeniami (kompensacja), a zmiany formatu przez upcasting.
- Zapytania wymagają modelu odczytu (CQRS jest praktycznie obowiązkowy).
- Port `CardRepository` i use case'y nie zmieniły się. Zmienił się adapter i wnętrze agregatu.
