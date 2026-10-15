# 03 – Porównanie architektur: warstwowa, onion, heksagonalna, czysta

> Moduł 1: Dlaczego architektura · poprzedni: `02-use-cases-and-ports` · następny: `04-cross-cutting-by-hand`

## Cel
Zobaczyć **ten sam przypadek użycia** („doładuj kartę”) w czterech stylach i zrozumieć, że onion,
heksagonalna i czysta architektura różnią się nazewnictwem i akcentami, ale mają **tę samą regułę zależności**:
zależności wskazują do środka, w stronę logiki biznesowej.

## Co się zmieniło
```bash
git diff 02-use-cases-and-ports..03-architectures-compared --stat
```
- `src/main/java/pl/fullstackdeveloper/compare/{layered,onion,hexagonal,clean}`: cztery samodzielne warianty.
- `src/test/java/pl/fullstackdeveloper/compare/*ArchitectureTest`: reguły ArchUnit opisujące każdy styl i test działania.
- `payments/ArchitectureTest`: pierwsze reguły ArchUnit dla właściwej aplikacji.

## Porównanie
| | Warstwowa | Onion | Heksagonalna (Ports & Adapters) | Czysta (Clean) |
|---|---|---|---|---|
| Autor / rok | klasyka n-tier | J. Palermo, 2008 | A. Cockburn, 2005 | R. C. Martin, 2012 |
| Kierunek zależności | z góry w dół, logika → baza | do środka | do środka | do środka |
| Gdzie interfejs repozytorium | w warstwie danych | domain services | port wyjściowy w aplikacji | gateway w use cases |
| Akcent | podział techniczny | pierścienie modelu | symetria: porty wejściowe i wyjściowe, wymienne adaptery | granice (boundaries), presenter, przepływ vs zależności |
| Reguła ArchUnit | `layeredArchitecture()` | `onionArchitecture()` | `noClasses()...adapter` | `layeredArchitecture()` z pierścieniami |

## Na co zwrócić uwagę
- **Warstwowa**: `CardService` importuje `CardDao`, czyli logika zależy od persystencji. To jedyny wariant, w którym tak jest.
- **Onion**: interfejs `CardRepository` należy do pierścienia `domain.services`.
- **Heksagonalna**: wąskie porty `LoadCardPort` i `SaveCardPort` (ISP). Kontroler zna tylko port wejściowy `TopUpUseCase`.
- **Czysta**: interaktor niczego nie zwraca. Wynik trafia do prezentera przez `TopUpOutputBoundary`, więc sterowanie płynie na zewnątrz, a zależność wskazuje do środka.
- W dalszej części szkolenia używamy stylu z `02` (heksagonalny z pakietami `domain/application/adapters`). Zasady są te same.

## Kiedy NIE stosować
Pełny zestaw boundary, presenter i view model z Clean Architecture opłaca się przy wielu kanałach prezentacji.
Dla REST API zwykle wystarczy, że use case zwraca model wyjściowy.

## Pytania do dyskusji
- Który styl jest najbliższy temu, co macie dziś w projektach?
- Czy różnica między onion a heksagonalną to coś więcej niż nazwy pakietów?

---

## Ćwiczenie „blog”, część 1 (koniec modułu 1)
Zbuduj **domenę i warstwę aplikacji** bloga w czystej Javie, wzorując się na `payments`.

Przypadki użycia autora: utwórz artykuł, zmień artykuł, opublikuj artykuł, usuń artykuł.
Przypadki użycia czytelnika: polub / nie lub artykułu, dodaj komentarz, znajdź artykuły po kategorii, znajdź artykuły po tagach.

Wymagania:
1. Agregat `Article` pilnuje niezmienników, np. nie można komentować nieopublikowanego artykułu, a tytuł jest wymagany. VO: `ArticleId`, `Title`, `Tag`, `Category`.
2. Porty wejściowe (`*UseCase` + komendy) i wyjściowe (`ArticleRepository`, `DateTimeProvider`).
3. Adaptery in-memory i composition root w `main()`.
4. Testy domeny i use case'ów bez frameworka oraz reguła ArchUnit „domain nie zależy od application/adapters”.

## Uruchomienie
```bash
./mvnw test
./mvnw -q compile && java -cp target/classes pl.fullstackdeveloper.compare.clean.frameworks.ConsoleApp
```
