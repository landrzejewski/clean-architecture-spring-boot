# 00 – Architektura warstwowa i anemiczny model

> Moduł 1: Dlaczego architektura · następny krok: `01-rich-domain`

## Cel
Pokazać typową aplikację Spring Boot zbudowaną „na skróty”: `Controller → Service → Repository`, w której
encja JPA jest jednocześnie modelem domeny, kontraktem REST i strukturą tabeli. To punkt wyjścia do całego szkolenia.

## Co tu jest
| Warstwa | Klasa | Rola |
|---|---|---|
| prezentacja | `controller/CardController` | REST, zwraca encje JPA |
| logika | `service/CardService` | Transaction Script, cała logika biznesowa w `if`-ach |
| dane | `repository/CardRepository` | Spring Data JPA |
| model | `model/CardEntity`, `model/TransactionEntity` | anemiczne encje (gettery/settery) |

Pakiety odpowiadają warstwom technicznym (package-by-layer). Zależności płyną w dół: kontroler → serwis → repozytorium → encje.

## Problemy do znalezienia w kodzie
1. **Reguły biznesowe są rozproszone i łatwe do obejścia.** Każdy może wywołać `card.setBalance(...)`, bo encja niczego nie chroni.
2. **Logika zależy od infrastruktury**: JPA, `ZonedDateTime.now()`, `Random`, `System.out`. Test reguły „brak środków” wymaga `@SpringBootTest` i bazy (`CardServiceTest`).
3. **Brak języka domeny**: `"IN"`/`"OUT"`, `BigDecimal` + `String currencyCode` zamiast `Money`, `IllegalStateException` zamiast wyjątków domenowych.
4. **Kontrakt REST = schemat bazy.** Dodanie kolumny zmienia JSON zwracany klientom, a w odpowiedzi wycieka `id` z bazy.
5. **Serwis rośnie bez końca.** Każda nowa funkcja (prowizje, limity, blokada karty) to kolejne `if`-y w `CardService`.

## Ćwiczenie (15 min)
Dodaj **prowizję 2% za płatność (`OUT`) w walucie innej niż waluta karty**, przeliczaną po kursie z zewnętrznego źródła.
Zanotuj, ile klas musisz zmienić, jak to przetestujesz i co się stanie, gdy kurs będzie pochodził z zewnętrznego API.

## Kiedy taki układ jest w porządku
Proste CRUD-y, prototypy, aplikacje z minimalną logiką biznesową. Architektura warstwowa nie jest zła sama w sobie.
Problem zaczyna się, gdy logika biznesowa rośnie, a model jej nie wyraża.

## Pytania do dyskusji
- Która zmiana w tej aplikacji byłaby najdroższa: zmiana bazy, dodanie nowego kanału (np. kolejki) czy nowa reguła biznesowa?
- Dlaczego test jednej reguły trwa kilka sekund?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run   # potem żądania z client.http
```
