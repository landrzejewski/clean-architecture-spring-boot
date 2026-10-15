# 1. Zapisujemy decyzje architektoniczne (ADR)

- Status: zaakceptowana
- Branch: `02-use-cases-and-ports`

## Kontekst
Decyzje architektoniczne giną w rozmowach i commitach. Nowa osoba w zespole nie wie, *dlaczego* kod wygląda tak, a nie inaczej.

## Decyzja
Każdą istotną decyzję zapisujemy jako krótki dokument ADR (Architecture Decision Record) w `docs/adr`:
kontekst, decyzja, konsekwencje. ADR-ów nie edytujemy wstecz. Zmiana decyzji to nowy ADR, który zastępuje stary.

## Konsekwencje
- Historia decyzji jest wersjonowana razem z kodem.
- Każdy branch szkolenia dopisuje ADR, gdy podejmuje decyzję wartą zapamiętania.
