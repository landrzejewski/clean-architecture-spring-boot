---
marp: true
theme: theory
paginate: true
lang: pl
title: Logika biznesowa i architektura
---

<!-- _class: title -->
<!-- _paginate: false -->

# Logika biznesowa i architektura

---

# Logika biznesowa w centrum systemu

System informatyczny powstaje po to, żeby realizować reguły biznesu. Framework, baza danych, protokół komunikacji czy podział na usługi to tylko środki techniczne, które z czasem się zmieniają.

<div class="chain">
<div class="box core">reguły biznesowe</div><div class="arr">→</div>
<div class="box soft">use case'y</div><div class="arr">→</div>
<div class="box">porty (kontrakty)</div><div class="arr">→</div>
<div class="box">adaptery</div><div class="arr">→</div>
<div class="box warm">technologie i inne systemy</div>
</div>

- Reguły biznesowe zmieniają się rzadziej niż technologia i stanowią unikalną wiedzę firmy. Biblioteki i frameworki są dostępne dla wszystkich i z czasem się starzeją.
- Dobra architektura umieszcza logikę w środku, a resztę układa wokół niej tak, żeby można ją było wymienić bez zmiany reguł.
- Ta zasada obowiązuje na każdym poziomie: w pojedynczej klasie, w module aplikacji i w systemie złożonym z wielu usług.

<div class="takeaway">

Przykład, do którego będziemy wracać: system **kart płatniczych** (dodawanie transakcji, przeliczanie walut, prowizje) oraz współpracujące z nim **zamówienia** i **wysyłki**.

</div>

---

# Reguła biznesowa, proces biznesowy, logika biznesowa

Te trzy pojęcia często się ze sobą myli, choć każde oznacza coś innego.

<div class="cols-3">
<div class="card">

## Reguła biznesowa

Zasada, według której działa firma. Obowiązuje niezależnie od systemu informatycznego, na przykład jako zapis w regulaminie.

Przykłady: „płatność nie może przekroczyć salda karty”, „płatność w obcej walucie jest obciążona prowizją 2%”.

</div>
<div class="card warm">

## Proces biznesowy

Powtarzalny ciąg kroków prowadzący do celu biznesowego. Często obejmuje kilka działów lub systemów.

Przykład: przyjęcie zamówienia, obciążenie karty, zlecenie wysyłki, potwierdzenie dla klienta.

</div>
<div class="card good">

## Logika biznesowa

Część aplikacji, która realizuje reguły i kroki procesów: sprawdza saldo, pobiera kurs waluty, nalicza prowizję, zmienia stan danych.

</div>
</div>

<div class="takeaway">

Stawka VAT jest regułą biznesową. Sposób naliczania podatku na pozycjach faktury, z zaokrągleniami i sumowaniem, to logika biznesowa. Reguły ustala biznes, a logikę piszą programiści, którzy muszą ją dostosowywać do każdej zmiany reguł.

</div>

---

# Dwa tempa zmian: biznes i technologia

Logika biznesowa i technologia zmieniają się z różnych powodów. Jeśli są ze sobą splecione, każda zmiana po jednej stronie wymusza zmiany po drugiej.

<div class="cols">
<div>

**Logika biznesowa zmienia się, gdy:**
- zmieniają się przepisy, rynek, oferta lub strategia firmy,
- pojawiają się klienci z nowymi wymaganiami,
- zespół lepiej rozumie dziedzinę, w której działa firma.

Logika powinna być czytelna i łatwa do przetestowania, bo zmienia się często, a błędy w niej są kosztowne.

</div>
<div>

**Technologia zmienia się, gdy:**
- biblioteka lub framework traci wsparcie,
- trzeba wymienić bazę danych albo dodać nowy kanał dostępu (aplikację mobilną, integrację z partnerem),
- rosną wymagania wydajnościowe.

Takie zmiany nie powinny wymagać modyfikacji reguł, bo reguły pozostały takie same.

</div>
</div>

<div class="takeaway">

Elementy, które zmieniają się z różnych powodów i w różnym tempie, należy od siebie oddzielić. To zasada pojedynczej odpowiedzialności zastosowana do całej aplikacji. Jej naruszenie widać wtedy, gdy reguły są rozproszone po kontrolerach, zapytaniach SQL i triggerach.

</div>

---

# Sprzężenie i spójność

Większość dobrych praktyk projektowych sprowadza się do dwóch celów: niskiego sprzężenia między elementami i wysokiej spójności wewnątrz nich.

<div class="cols">
<div class="card">

## Sprzężenie (coupling)

Określa, w jakim stopniu zmiana jednego elementu wymusza zmiany w innych. Sprzężenie rośnie, gdy elementy:
- znają nawzajem swoją wewnętrzną budowę,
- współdzielą dane, typy lub transakcje,
- muszą działać w tym samym czasie.

Przy wysokim sprzężeniu drobna zmiana pociąga za sobą zmiany w wielu miejscach.

</div>
<div class="card good">

## Spójność (cohesion)

Określa, w jakim stopniu elementy jednej jednostki służą wspólnemu celowi. Wysoka spójność oznacza, że:
- to, co zmienia się razem, znajduje się w jednym miejscu,
- jednostka ma jedną, jasno nazwaną odpowiedzialność,
- każdą regułę można znaleźć w jednym miejscu.

Przy niskiej spójności powstają klasy i moduły, które robią wszystko po trochu.

</div>
</div>

<div class="takeaway">

Model domeny, porty i adaptery, moduły i usługi to narzędzia, które służą tym samym celom, tylko na coraz wyższym poziomie.

</div>

---

# Kontrakt: interfejs na każdym poziomie

Interfejs (API) określa, co można zrobić z danym elementem, i ukrywa, w jaki sposób zostało to zrealizowane. Dopóki kontrakt się nie zmienia, obie strony mogą rozwijać się niezależnie.

| Poziom | Granica | Przykład kontraktu |
|---|---|---|
| obiekt | metody publiczne | `Card.registerTransaction(...)` zamiast publicznego settera salda |
| komponent | interfejs w kodzie | `CardRepository`, `AddTransactionUseCase` |
| moduł | publiczny pakiet lub artefakt | API modułu płatności i publikowane przez niego zdarzenia |
| usługa sieciowa | opis protokołu | WSDL, OpenAPI, schemat GraphQL, plik `.proto` |
| proces asynchroniczny | format komunikatu | schemat zdarzenia publikowanego w brokerze |

- Im dalej od środka systemu, tym więcej klientów korzysta z kontraktu i tym droższa jest jego zmiana.
- Dlatego kontrakty zewnętrzne opisuje się formalnie i wersjonuje.

<div class="takeaway">

Dobry kontrakt jest wąski, używa pojęć zrozumiałych dla odbiorcy i nie ujawnia szczegółów implementacji, takich jak struktura tabel czy nazwy klas frameworka.

</div>

---

# Wzorce projektowe i fasada

Wzorzec projektowy to nazwane i sprawdzone rozwiązanie problemu, który powtarza się w podobnych sytuacjach. Pojęcie wprowadził architekt Christopher Alexander (1977), a do programowania obiektowego przenieśli je autorzy książki *Design Patterns* (1994), nazywani „bandą czworga” (GoF).

<div class="cols-wide">
<div>

- Największą zaletą wzorców jest **wspólny słownik**. Zdanie „to jest fasada modułu” mówi więcej niż opis kilku klas.
- Wzorzec stosuje się wtedy, gdy rozwiązuje rzeczywisty problem, a nie dla samej zasady.
- **Fasada** udostępnia jeden prosty punkt wejścia do całego podsystemu. Klient zna tylko fasadę, więc wie mniej o wnętrzu podsystemu i jest z nim słabiej powiązany. Publiczne API modułu jest w praktyce fasadą.

</div>
<div>

| Wzorzec | Gdzie się pojawi |
|---|---|
| Facade | publiczne API modułu |
| Adapter | porty i adaptery |
| Proxy, Decorator | aspekty, klienci usług zdalnych |
| Observer | zdarzenia |
| Factory | tworzenie i odtwarzanie obiektów |
| Strategy | wymienne implementacje |

</div>
</div>

---

# Inversion of Control i Dependency Injection

Klasa, która sama tworzy swoje zależności, jest z nimi na stałe związana. Nie da się wtedy podmienić implementacji ani przetestować klasy w izolacji. Rozwiązaniem jest przekazanie tej odpowiedzialności na zewnątrz.

<div class="cols">
<div>

**Inversion of Control (IoC)** oznacza, że obiekt nie zarządza sam swoim cyklem życia ani przebiegiem programu. Robi to za niego ktoś z zewnątrz, zgodnie z zasadą „nie dzwoń do nas, to my zadzwonimy”.

**Dependency Injection (DI)** to najczęstsza realizacja IoC. Zależności są tworzone i konfigurowane poza klasą, a następnie do niej przekazywane, zwykle przez konstruktor.

</div>
<div>

Zależności można dostarczać:
- **ręcznie**, składając obiekty w jednym miejscu programu,
- **za pomocą wzorców**, np. fabryki, która buduje cały graf obiektów,
- **za pomocą kontenera** (Spring, CDI), który zarządza też cyklem życia komponentów.

Miejsce, w którym składa się graf obiektów, nazywa się **composition root**. Tylko tam znane są konkretne implementacje.

</div>
</div>

<div class="takeaway">

Dzięki temu sprzężenie maleje, implementacje można wymieniać, a w testach łatwo podstawić prostą, kontrolowaną wersję zależności.

</div>

---

# Dependency Inversion Principle

Dependency Injection odpowiada na pytanie, jak dostarczyć zależność. Dependency Inversion Principle (DIP) odpowiada na pytanie, w którą stronę zależność powinna być skierowana.

<div class="cols">
<div>

<div class="vchain">
<div class="box bad">logika → klasa repozytorium JPA<span class="sub">reguły zależą od szczegółu technicznego</span></div>
<div class="arr">↓ odwracamy kierunek</div>
<div class="box good">logika → <code>CardRepository</code> ← adapter JPA<span class="sub">obie strony zależą od abstrakcji</span></div>
</div>

</div>
<div>

- Moduły wysokiego poziomu (reguły) nie powinny zależeć od modułów niskiego poziomu (baza danych, sieć). Obie strony powinny zależeć od abstrakcji.
- Abstrakcje nie powinny zależeć od szczegółów, tylko szczegóły od abstrakcji.
- Kluczowe jest to, **kto jest właścicielem** interfejsu. Definiuje go strona, która go potrzebuje (logika), a nie strona, która go implementuje (infrastruktura).

</div>
</div>

<div class="takeaway">

Wywołanie biegnie od logiki do bazy danych, ale zależność w kodzie jest skierowana odwrotnie: od adaptera bazy do logiki. Na tej obserwacji opierają się wszystkie architektury, w których logika znajduje się w centrum.

</div>

---

# Co oferuje kontener DI

Kontener taki jak Spring czy CDI nie tylko tworzy obiekty. Pozwala też dodawać do nich zachowanie i wybierać jedną z kilku implementacji tego samego interfejsu.

<div class="cols">
<div class="card">

## Interceptor czy Decorator?

**Interceptor** obsługuje zadania techniczne, niezależne od typu obiektu: transakcje, logowanie, pomiar czasu. Przypisuje się go do metod adnotacją i nie musi znać ich interfejsu.

**Decorator** implementuje ten sam interfejs co obiekt, który opakowuje. Rozszerza konkretne operacje biznesowe (np. dolicza prowizję), a następnie przekazuje wywołanie do oryginalnego obiektu.

</div>
<div class="card good">

## Wybór implementacji

Gdy interfejs ma kilka implementacji, kontener musi wiedzieć, którą wstrzyknąć. Służą do tego:
- **kwalifikatory** (`@Qualifier`), czyli adnotacje wskazujące wariant,
- **alternatywy** (`@Alternative`) i profile, czyli inne implementacje dla różnych środowisk,
- **warunki** (np. `@ConditionalOnProperty`), dzięki którym o wyborze decyduje konfiguracja.

</div>
</div>

<div class="takeaway">

Ten sam mechanizm pozwala później przełączać technologie: repozytorium JPA lub JDBC, stałe kursy walut lub kursy pobierane z API NBP.

</div>

---

# Cross-cutting concerns i AOP

Transakcje, logowanie, pomiar czasu, retry i kontrola dostępu dotyczą wielu use case'ów, ale nie są logiką biznesową. Programowanie aspektowe (AOP) pozwala zdefiniować je raz i stosować deklaratywnie.

<div class="cols-wide">
<div>

- Bez takiego wydzielenia kod techniczny powtarza się w wielu klasach i przeplata z regułami, przez co trudniej go czytać i zmieniać.
- Aspekt wyraża intencję, np. „ta operacja ma być atomowa” albo „przy konflikcie spróbuj ponownie”, i nie wiąże logiki z konkretną technologią.
- AOP realizuje się najczęściej za pomocą wzorca **Proxy**. Obiekt pośredniczący przechwytuje wywołanie, wykonuje dodatkowe czynności i przekazuje wywołanie dalej.
- **Kolejność aspektów ma znaczenie.** Retry musi obejmować całą transakcję. W przeciwnym razie ponowna próba zostałaby wykonana w transakcji, która została już wycofana.

</div>
<div>

<div class="stack">
<div class="dark">wywołujący</div>
<div class="down">↓</div>
<div>proxy: retry</div>
<div>proxy: pomiar czasu</div>
<div>proxy: transakcja</div>
<div class="down">↓</div>
<div class="hot">use case (sama logika)</div>
</div>

<p class="small mt">Ograniczenia proxy: wywołanie metody z wnętrza tej samej klasy omija pośrednika (self-invocation), a przechwytywane metody i klasy nie mogą być finalne.</p>

</div>
</div>

---

# Programowanie przez zdarzenia

Zamiast bezpośrednio wywoływać kolejne komponenty, obiekt ogłasza, że coś się wydarzyło. Zainteresowane komponenty same na to reagują. To wzorzec Observer, który skutecznie zmniejsza powiązania między elementami.

<div class="cols">
<div>

- Nadawca nie wie, ilu jest odbiorców ani kto obsłuży zdarzenie. Nowego odbiorcę można dodać bez zmiany nadawcy.
- Zdarzenie opisuje **fakt, który już nastąpił**, np. „transakcja zarejestrowana” albo „płatność przetworzona”, i zawiera dane potrzebne odbiorcom.
- Tę samą ideę stosuje się wewnątrz modelu domeny, między modułami jednej aplikacji i między usługami komunikującymi się przez broker.

</div>
<div class="card warm">

## Cena luźnych powiązań

- Przebiegu operacji nie widać już bezpośrednio w kodzie. Trudniej odpowiedzieć na pytanie, co się stanie po jej wykonaniu.
- Błędy odbiorców trzeba obsługiwać osobno, bo nadawca zakończył już pracę.
- Zdarzenie przechowywane tylko w pamięci może zginąć, jeśli awaria nastąpi po zapisie danych, a przed publikacją zdarzenia.

</div>
</div>

<div class="takeaway">

Problemem gubionych zdarzeń zajmiemy się przy integracji usług. Rozwiązuje go wzorzec Transactional Outbox.

</div>

---

# Domain-Driven Design

Skoro logika biznesowa jest najcenniejszą częścią systemu, warto wyrazić ją w kodzie wprost. Domain-Driven Design (Eric Evans, 2003) to podejście, w którym obiekty i ich zachowania wiernie odwzorowują dziedzinę problemu.

<div class="cols">
<div>

Założenia DDD:
- **model domeny** jest centralną częścią oprogramowania, a nie dodatkiem do bazy danych,
- programiści współpracują z **ekspertami domenowymi** od początku projektu,
- najpierw trzeba zrozumieć problem biznesowy, a dopiero potem go implementować,
- model rozwija się razem z wiedzą zespołu o dziedzinie.

</div>
<div>

<div class="card">

## Strategic design

Dotyczy podziału dużego problemu: wyznaczenia kontekstów (Bounded Contexts), ich granic i relacji między nimi.

</div>

<div class="card good mt">

## Tactical design

Dotyczy budowy modelu wewnątrz kontekstu: encji, Value Objects, agregatów, repozytoriów, Domain Services i Domain Events.

</div>

</div>
</div>

---

# Ubiquitous Language

Ubiquitous Language to wspólny język ekspertów domenowych, analityków i programistów. Używa się go konsekwentnie w rozmowach, dokumentacji, testach i w nazwach w kodzie, dzięki czemu kod bezpośrednio odzwierciedla model biznesowy.

<div class="cols-wide">
<div>

- Jednoznaczne pojęcia ograniczają błędy w interpretacji wymagań, bo ekspert i programista mówią o tym samym.
- Język rozwija się razem z wiedzą zespołu. Gdy zmieniają się pojęcia, zmieniają się też nazwy w kodzie.
- Język obejmuje też standardowe pojęcia modelu: encje, agregaty, Value Objects i repozytoria.
- **Brak wspólnego języka** widać po klasach typu `CardService.process()` czy `DataHelper`, po kilku nazwach tego samego pojęcia i po regułach ukrytych w nazwach kolumn i flagach.

</div>
<div class="card">

## Język należy do kontekstu

W domenie płatności transakcja jest **wpływem** albo **płatnością** (`INFLOW`, `PAYMENT`).

Publiczne API dla klientów może używać prostszych oznaczeń `IN` i `OUT`.

To dwa języki dla dwóch różnych odbiorców. Tłumaczenie między nimi jest zadaniem adaptera, a nie modelu domeny.

</div>
</div>

---

# Bounded Context

Duży system nie ma jednego wspólnego modelu. Dzieli się go na konteksty (Bounded Contexts), z których każdy ma własny model dopasowany do swoich potrzeb, warunków i procesów.

<div class="chain">
<div class="box core">Płatności<span class="sub">karta: saldo, waluta, termin ważności, transakcje</span></div>
<div class="arr">⇄</div>
<div class="box soft">Zamówienia<span class="sub">karta: tylko numer do obciążenia</span></div>
<div class="arr">⇄</div>
<div class="box soft">Wysyłki<span class="sub">zamówienie: adres i paczka</span></div>
</div>

- To samo słowo, np. „karta” czy „zamówienie”, w każdym kontekście oznacza coś innego i podlega innym regułom. Jeden model dla całej firmy staje się kompromisem, który nikomu dobrze nie służy.
- Konteksty komunikują się przez **jasno określony interfejs**. Zmiany modelu i logiki wewnątrz kontekstu nie mogą tego interfejsu naruszać.
- Relacje między kontekstami mają ustalone nazwy: Customer–Supplier, Conformist, Shared Kernel, Published Language oraz **Anti-Corruption Layer**, który tłumaczy obcy model na własny.

<div class="takeaway">

Granice kontekstów to naturalne granice modułów aplikacji, a w razie potrzeby także osobnych usług.

</div>

---

# Model anemiczny i Transaction Script

W modelu anemicznym obiekty domeny są wyłącznie strukturami danych, a cała logika znajduje się w klasach serwisów. To popularne podejście, bo naturalnie wynika z projektowania systemu od strony tabel bazy danych.

<div class="cols">
<div>

**Jak wygląda:**
- encje odwzorowują tabele i mają gettery oraz settery dla każdego pola,
- obiekty DAO udostępniają operacje CRUD dla poszczególnych tabel,
- serwisy realizują logikę jako **Transaction Script**: pobierają dane, sprawdzają warunki i zapisują wynik.

**Kiedy wystarcza:** gdy logiki jest niewiele, a aplikacja składa się głównie z formularzy i raportów.

</div>
<div class="card bad">

## Problemy przy rosnącej logice

- Nic nie chroni niezmienników. Dowolny fragment kodu może ustawić dowolne saldo.
- Ta sama reguła powtarza się w kilku serwisach, więc każda jej zmiana wymaga przeszukania kodu.
- Ta sama klasa jest modelem domeny, modelem bazy i kontraktem API, więc zmiana schematu bazy wpływa na całą aplikację.
- Przetestowanie reguły wymaga uruchomienia bazy danych i kontenera aplikacji.

</div>
</div>

---

# Model bogaty

W modelu bogatym dane i zachowanie znajdują się w tych samych obiektach. Obiekty domeny same pilnują swoich reguł, a serwisy jedynie koordynują pracę.

<div class="cols">
<div>

- Większość logiki znajduje się w **agregatach, encjach i Value Objects**.
- Stan zmienia się wyłącznie przez metody biznesowe, np. „zarejestruj transakcję”, a nie przez settery. Obowiązuje zasada *Tell, Don't Ask*: mówimy obiektowi, co ma zrobić, zamiast pobierać jego dane i decydować za niego.
- **Repozytoria** obsługują całe agregaty, więc jest ich znacznie mniej niż obiektów DAO.
- **Serwisy** są proste: ładują agregat, wywołują jego metodę i zapisują wynik.

</div>
<div>

| | Model anemiczny | Model bogaty |
|---|---|---|
| encje | struktury danych | zachowanie i niezmienniki |
| logika | w serwisach | w modelu domeny |
| repozytoria | po jednym na tabelę | po jednym na agregat |
| serwisy | rozbudowane | proste, koordynujące |
| testy | integracyjne, wolne | jednostkowe, szybkie |

</div>
</div>

<div class="takeaway">

Model bogaty opłaca się wtedy, gdy reguł jest dużo, często się zmieniają, a błędy są kosztowne. Tak jest w przypadku płatności.

</div>

---

# Building blocks modelu domeny

Tactical design w DDD wyróżnia kilka rodzajów obiektów. Każdy z nich ma inną rolę w zakresie tożsamości, spójności i odpowiedzialności.

| Element | Cechy | Przykład |
|---|---|---|
| **Encja** (Entity) | ma tożsamość, która trwa mimo zmian stanu | transakcja z własnym identyfikatorem |
| **Value Object** | nie ma tożsamości, jest niezmienny, porównuje się go po wartości i sam pilnuje swojej poprawności | kwota z walutą, numer karty, kurs wymiany |
| **Agregat** (Aggregate) | grupa obiektów z korzeniem (aggregate root), która stanowi granicę spójności | karta wraz z jej transakcjami |
| **Repozytorium** (Repository) | zapisuje i odczytuje całe agregaty, ukrywając sposób przechowywania | repozytorium kart |
| **Domain Service** | realizuje regułę, która nie pasuje do żadnego pojedynczego obiektu | przeliczenie waluty z prowizją |
| **Domain Event** | opisuje fakt, który zaszedł w domenie | „transakcja zarejestrowana” |
| **Fabryka** (Factory) | tworzy złożone obiekty albo odtwarza je z zapisanego stanu | odtworzenie karty z bazy bez ponownej walidacji |

<div class="takeaway">

Value Objects chronią przed *primitive obsession*. Zamiast liczby i napisu mamy typ, który nie pozwala utworzyć ujemnej kwoty ani dodać do siebie kwot w różnych walutach.

</div>

---

# Agregat jako granica spójności

Agregat to grupa obiektów, które zmieniają się zawsze razem. Kod spoza agregatu ma dostęp tylko do jego korzenia (aggregate root) i tylko przez niego może zmieniać stan.

<div class="cols-wide">
<div>

- **Niezmienniki**, np. „saldo po płatności nie może być ujemne”, są sprawdzane wewnątrz agregatu, więc nie da się ich obejść.
- Przyjmuje się zasadę, że **jedna transakcja bazodanowa zmienia jeden agregat**. Ogranicza to zakres blokad i ryzyko konfliktów.
- Do innych agregatów odwołujemy się przez **identyfikator**, a nie przez referencję do obiektu.
- Między agregatami obowiązuje **eventual consistency**: spójność osiąga się przez zdarzenia, a nie przez wspólną transakcję.
- Agregaty powinny być **małe**. Duży agregat oznacza więcej konfliktów przy równoległych zmianach i wolniejsze ładowanie.

</div>
<div>

<div class="vchain">
<div class="box core">Karta (aggregate root)<span class="sub">numer, waluta, saldo, termin ważności</span></div>
<div class="arr">↓ zawiera</div>
<div class="box soft">Transakcje<span class="sub">identyfikator, czas, kwota, typ</span></div>
<div class="arr">↓ publikuje</div>
<div class="box warm">transakcja zarejestrowana</div>
</div>

<p class="small mt">Granica agregatu wyznacza też zakres optimistic locking (numer wersji), jednostkę zapisu, a w Event Sourcingu jeden strumień zdarzeń.</p>

</div>
</div>

---

# Domena a infrastruktura

Model domeny powinien być niezależny od frameworka, bazy danych i protokołu. Aplikacja musi jednak zapisywać dane, pobierać kursy walut i publikować zdarzenia. Oba wymagania da się pogodzić dzięki odwróceniu zależności.

<div class="cols">
<div>

**Domena nie korzysta bezpośrednio z:**
- adnotacji frameworka, mapowania ORM ani HTTP,
- zegara systemowego i generatora liczb losowych (czas i identyfikatory otrzymuje z zewnątrz),
- konkretnego formatu zapisu i sposobu komunikacji.

Dzięki temu jest **deterministyczna**: ten sam stan i ta sama komenda zawsze dają ten sam wynik. Testy są wtedy proste i szybkie.

</div>
<div>

**Współpraca z infrastrukturą:**
- domena i warstwa aplikacji definiują interfejsy: repozytorium, zegar, dostawcę kursów, wydawcę zdarzeń,
- infrastruktura dostarcza ich implementacje,
- zadania techniczne dodaje się za pomocą wzorców Proxy i Decorator,
- konteksty komunikują się asynchronicznie przez zdarzenia.

</div>
</div>

<div class="takeaway">

Technologię można wtedy zmieniać bez wpływu na logikę. Trzeba jeszcze tak zorganizować całą aplikację, żeby ta zasada była przestrzegana trwale, a nie tylko dzięki dyscyplinie zespołu. Tym zajmuje się architektura.

</div>

---

# Czym jest architektura

Architektura określa najważniejsze komponenty rozwiązania, ich odpowiedzialność, wzajemne relacje i sposób komunikacji. Jest szablonem rozwiązania, a nie jego szczegółowym projektem.

<div class="cols-wide">
<div>

- Architekturę opisuje się na kilku poziomach: **aplikacji** (warstwy, moduły, kierunek zależności), **systemu** (usługi i ich komunikacja) oraz **infrastruktury** (serwery, sieć, chmura).
- W praktyce architekturę tworzą decyzje, których późniejsza zmiana jest **kosztowna**: kierunek zależności, granice modułów, sposób komunikacji, model danych.
- Architektura nie jest celem samym w sobie. Ma umożliwić spełnienie wymagań funkcjonalnych i jakościowych przy rozsądnym koszcie zmian.
- Dobra architektura pozwala odłożyć decyzje o szczegółach (bazie, frameworku, protokole) do chwili, w której wiadomo o nich najwięcej.

</div>
<div>

<div class="stack">
<div class="dark">infrastruktura</div>
<div>system: usługi, kontrakty, komunikacja</div>
<div>aplikacja: warstwy, porty, moduły</div>
<div class="hot">model domeny</div>
</div>

<p class="small mt">Kolejne slajdy omawiają te poziomy od środka na zewnątrz: od organizacji pojedynczej aplikacji, przez podział systemu, aż po protokoły komunikacji.</p>

</div>
</div>

---

# Wymagania niefunkcjonalne a architektura

Wymagania funkcjonalne określają, co system ma robić. Wymagania niefunkcjonalne (atrybuty jakości) określają, jak dobrze ma to robić. Zależą od rodzaju aplikacji, celów biznesowych, budżetu i użytkowników, a na kształt architektury często wpływają bardziej niż same funkcje.

| Wymaganie | Typowa konsekwencja dla architektury |
|---|---|
| dostępność 24/7/365 | redundancja, brak pojedynczego punktu awarii, wdrożenia bez przestojów |
| 1000 równoczesnych użytkowników | bezstanowe instancje aplikacji, skalowanie poziome, cache |
| znacznie więcej odczytów niż zapisów, złożone widoki | osobny model odczytu (CQRS) |
| częste zmiany reguł | czytelny, dobrze przetestowany model domeny, czasem silnik reguł |
| kilka niezależnych zespołów | wyraźne granice modułów, w razie potrzeby osobne usługi |
| integracja z systemami zewnętrznymi | stabilne, wersjonowane kontrakty i Anti-Corruption Layer |

<div class="takeaway">

Architektury nie wybiera się według mody ani upodobań zespołu. O wyborze decyduje to, które atrybuty jakości są dla danego systemu najważniejsze i ile kosztuje ich zapewnienie.

</div>

---

# Architektura warstwowa

To najbardziej znany styl architektury. Komponenty są pogrupowane w warstwy techniczne o określonych rolach. Każda warstwa korzysta tylko z warstwy znajdującej się pod nią.

<div class="cols">
<div>

<div class="stack">
<div>prezentacja (kontrolery, widoki)</div>
<div class="down">↓</div>
<div>logika biznesowa (serwisy)</div>
<div class="down">↓</div>
<div class="hot">dostęp do danych (repozytoria, encje ORM)</div>
</div>

<ul class="pros mt">
<li>prosta i powszechnie znana</li>
<li>wystarczająca, gdy logiki jest niewiele</li>
</ul>

</div>
<div>

<ul class="cons">
<li><strong>Logika zależy od warstwy danych.</strong> Zmiana bazy danych lub ORM wymaga zmian w kodzie reguł biznesowych.</li>
<li>Model domeny upodabnia się do schematu tabel i zwykle staje się anemiczny.</li>
<li>Nowa funkcja wymaga zmian we wszystkich warstwach, bo podział jest techniczny, a nie biznesowy.</li>
<li>Do przetestowania reguły potrzebna jest baza danych, więc testy są wolne i niestabilne.</li>
</ul>

</div>
</div>

<div class="takeaway">

Problemem nie są same warstwy, tylko **kierunek zależności**: najważniejsza warstwa zależy od najmniej stabilnej. Wystarczy ten kierunek odwrócić.

</div>

---

# Porty i adaptery (architektura heksagonalna)

Alistair Cockburn (2005) zaproponował, żeby aplikacja nie wiedziała, czy wywołuje ją człowiek, test, czy inny system, ani gdzie są przechowywane jej dane. Logika znajduje się w środku i komunikuje się ze światem zewnętrznym przez porty i adaptery.

<div class="chain">
<div class="stackcol"><div class="box">REST</div><div class="box">import CSV</div><div class="box">test</div></div>
<div class="arr">→</div>
<div class="box soft">port wejściowy<span class="sub"><code>AddTransactionUseCase</code></span></div>
<div class="arr">→</div>
<div class="box core">aplikacja i domena</div>
<div class="arr">→</div>
<div class="box soft">porty wyjściowe<span class="sub"><code>CardRepository</code>, <code>ExchangeRateProvider</code></span></div>
<div class="arr">←</div>
<div class="stackcol"><div class="box">JPA / JDBC</div><div class="box">API NBP</div><div class="box">Kafka</div></div>
</div>

<div class="cols">
<div>

- **Port** to interfejs, który wyraża intencję językiem aplikacji, np. „dodaj transakcję” albo „znajdź kartę”.
- **Adapter** realizuje tę intencję za pomocą konkretnej technologii: HTTP, SQL czy brokera komunikatów.

</div>
<div>

- **Adaptery driving** (primary) wywołują aplikację: kontroler, konsument kolejki, zadanie cykliczne.
- **Adaptery driven** (secondary) są wywoływane przez aplikację: baza danych, zewnętrzne API, broker.

</div>
</div>

---

# Onion Architecture i Clean Architecture

Jeffrey Palermo (Onion Architecture, 2008) i Robert C. Martin (Clean Architecture, 2012) przedstawili tę samą ideę w postaci koncentrycznych pierścieni. Najważniejsze reguły są w środku, a szczegóły techniczne na zewnątrz.

<div class="cols">
<div>

<div class="rings">
  <div class="r4">Frameworks &amp; Drivers</div>
  <div class="r3">Interface Adapters</div>
  <div class="r2">Use Cases</div>
  <div class="r1">Entities</div>
</div>

</div>
<div>

- W centrum znajdują się **reguły biznesowe przedsiębiorstwa** (Entities), wokół nich **reguły aplikacji** (Use Cases), dalej adaptery, a na zewnątrz frameworki, bazy danych i interfejs użytkownika.
- **Reguła zależności** (dependency rule): zależności w kodzie wskazują wyłącznie do środka. Kod w pierścieniu wewnętrznym nie zna żadnych nazw z pierścieni zewnętrznych.
- Wywołania mogą biec na zewnątrz (use case wywołuje repozytorium), ale zależność w kodzie nadal wskazuje do środka, bo interfejs repozytorium należy do pierścienia wewnętrznego.
- Przez granice pierścieni przekazuje się proste struktury danych.

</div>
</div>

---

# Cztery style, jedna reguła

Ten sam use case można zorganizować na cztery sposoby. Trzy z nich w dużej mierze się pokrywają i różnią się przede wszystkim nazewnictwem.

| | Warstwowa | Onion | Heksagonalna | Clean |
|---|---|---|---|---|
| kierunek zależności | w dół, do warstwy danych | do środka | do środka | do środka |
| gdzie jest interfejs repozytorium | w warstwie danych | w warstwie Domain Services | port wyjściowy | gateway w warstwie use case'ów |
| na co kładzie nacisk | podział techniczny | pierścienie modelu domeny | wymienne adaptery | granice i przepływ danych przez nie |

- Onion, architekturę heksagonalną i Clean Architecture łączy to, co najważniejsze: **logika biznesowa jest w środku, a zależności są skierowane do niej**.
- Nazwa stylu ma mniejsze znaczenie niż konsekwentne przestrzeganie reguły zależności i automatyczne sprawdzanie, czy jest przestrzegana.

<div class="takeaway">

Te architektury wymagają więcej klas i mapowań niż prosta architektura warstwowa. Ten koszt zwraca się tam, gdzie logika jest rozbudowana i ma długo żyć.

</div>

---

# Use case

Use case (application service) realizuje jedną intencję użytkownika. Sam nie zawiera reguł biznesowych, tylko koordynuje pracę modelu domeny i infrastruktury.

<div class="cols-wide">
<div>

Typowy przebieg:
1. przyjmuje **komendę**, czyli obiekt opisujący intencję, i sprawdza jej kompletność,
2. pobiera agregat przez port repozytorium,
3. wywołuje metodę biznesową agregatu, w której znajdują się reguły,
4. zapisuje agregat i publikuje powstałe zdarzenia,
5. zwraca **model wyjściowy**, a nie sam agregat.

Use case jest też naturalną **granicą transakcji** i miejscem, w którym stosuje się aspekty: transakcje, retry i pomiar czasu.

</div>
<div>

<div class="vchain">
<div class="box soft">komenda „dodaj transakcję”</div>
<div class="arr">↓</div>
<div class="box core">use case</div>
<div class="arr">↓ pobiera, wywołuje, zapisuje</div>
<div class="box">karta, repozytorium, zegar, wydawca zdarzeń</div>
<div class="arr">↓</div>
<div class="box soft">wynik: identyfikator transakcji</div>
</div>

<p class="small mt">Model wyjściowy chroni agregat: kod zewnętrzny nie może wywołać metody biznesowej z pominięciem use case'u.</p>

</div>
</div>

---

# Mapowanie modeli między warstwami

Jeśli domena, persystencja i API mają osobne modele, trzeba je mapować. To cena za niezależność warstw. Można wybrać jedną z kilku strategii, i to osobno dla każdej granicy.

| Strategia | Na czym polega | Zaleta | Wada |
|---|---|---|---|
| bez mapowania | jedna klasa jest encją bazy, modelem domeny i kontraktem API | najmniej kodu | zmiana schematu bazy zmienia API i reguły |
| dwukierunkowe | każda warstwa ma własny model, adapter mapuje w obie strony | pełna niezależność warstw | kod mapujący |
| pełne | osobny model dla każdej operacji, np. żądanie i komenda | precyzyjne kontrakty | najwięcej klas |
| jednokierunkowe | warstwy przekazują sobie wspólny typ tylko do odczytu | mniej mapowania | wspólny typ wiąże warstwy |

- Model persystencji powinien być ukryty w adapterze. Agregat jest z niego **odtwarzany** bez ponownej walidacji i bez publikowania zdarzeń, bo odtwarzany stan był już wcześniej poprawny.
- Model API używa języka klientów, a model domeny języka ekspertów. Tłumaczy je mapper w adapterze.

---

# Anti-Corruption Layer

Jeśli zewnętrzny system ma obcy model (inne pojęcia, inne nazwy, inną jakość danych), nie wpuszczamy tego modelu do domeny. Na granicy umieszczamy warstwę tłumaczącą: Anti-Corruption Layer (ACL).

<div class="cols">
<div>

<div class="vchain">
<div class="box dash">API kursów walut NBP<span class="sub">tabele kursów w formacie dostawcy</span></div>
<div class="arr">↓ adapter pełniący rolę ACL</div>
<div class="box core">domena: kurs wymiany jako Value Object</div>
</div>

</div>
<div>

- Chroni Ubiquitous Language przed przenikaniem cudzych pojęć do modelu.
- Ogranicza skutki zmian: gdy dostawca zmieni format danych, poprawia się tylko adapter.
- Pozwala testować domenę bez dostępu do sieci, bo zamiast prawdziwego dostawcy można użyć implementacji ze stałymi kursami.
- Stosuje się go zarówno wobec systemów zewnętrznych, jak i wobec innych kontekstów tej samej firmy. Na przykład zamówienia tłumaczą wynik płatności na własne pojęcia „opłacone” i „odrzucone”.

</div>
</div>

<div class="takeaway">

Anti-Corruption Layer to adapter z dodatkowym zadaniem: tłumaczy nie tylko technologię, ale też język.

</div>

---

# Framework jako szczegół

W Clean Architecture framework nie jest fundamentem aplikacji, lecz jednym z narzędzi używanych na jej obrzeżach. Domena i use case'y to zwykłe klasy, które można uruchomić i przetestować bez frameworka.

<div class="cols">
<div>

- Adnotacje frameworka występują w **adapterach** i w **konfiguracji**, która składa obiekty (composition root).
- Wymiana kontenera DI, aktualizacja frameworka czy zmiana serwera aplikacji nie wpływa na reguły biznesowe.
- Testy logiki nie wymagają uruchamiania kontekstu aplikacji, więc trwają milisekundy.
- Aspekty, takie jak transakcje czy retry, deklaruje się własnymi adnotacjami lub w konfiguracji, a framework tylko je realizuje.

</div>
<div class="card warm">

## Świadomy kompromis

Wiele zespołów dopuszcza w warstwie aplikacji np. adnotację transakcji z frameworka, bo pełna izolacja wymaga dodatkowego kodu.

Takie rozwiązanie jest w porządku, jeśli wynika ze świadomej **decyzji**, a nie z przypadku. Decyzję, jej powody i konsekwencje zapisuje się w ADR (Architecture Decision Record).

</div>
</div>

<div class="takeaway">

Clean Architecture nie zabrania używania frameworków. Określa jedynie, która część systemu od której zależy.

</div>

---

# Walidacja na trzech poziomach

Dane wejściowe sprawdza się w trzech miejscach, a każde z nich odpowiada na inne pytanie. Dzięki temu wiadomo, skąd pochodzi błąd i jak na niego zareagować.

<div class="cols-3">
<div class="card">

## Format

**Gdzie:** adapter wejściowy (DTO żądania).

**Pytanie:** czy kwota jest dodatnią liczbą, czy kod waluty składa się z trzech wielkich liter, czy wymagane pola są wypełnione?

**Błąd:** żądanie ma niepoprawną postać.

</div>
<div class="card warm">

## Kompletność

**Gdzie:** komenda use case'u.

**Pytanie:** czy komenda zawiera wszystkie dane potrzebne do jej wykonania i czy są one ze sobą zgodne?

**Błąd:** komenda jest niekompletna lub wewnętrznie sprzeczna.

</div>
<div class="card good">

## Reguły biznesowe

**Gdzie:** model domeny.

**Pytanie:** czy saldo wystarczy, czy karta jest ważna, czy waluta transakcji zgadza się z walutą karty?

**Błąd:** żądanie jest poprawne, ale narusza reguły biznesowe.

</div>
</div>

<div class="takeaway">

Domena nie zna kodów odpowiedzi HTTP. To adapter decyduje, że błąd formatu oznacza odpowiedź 400, a naruszenie reguły biznesowej odpowiedź 422. Wrócimy do tego przy omawianiu protokołów.

</div>

---

# Testy zgodne z architekturą

Architektura z logiką w centrum wpływa na sposób testowania. Im bliżej środka, tym więcej testów i tym są one szybsze, bo nie wymagają infrastruktury.

<div class="cols-wide">
<div>

| Poziom | Co sprawdzamy | Czego potrzeba |
|---|---|---|
| domena | reguły i niezmienniki | niczego poza kodem |
| aplikacja | przebieg use case'u | prostych implementacji portów |
| adapter wejściowy | mapowanie, walidację, kody odpowiedzi | warstwy webowej frameworka (slice test) |
| adapter wyjściowy | zapis, odczyt, integracje | bazy w kontenerze (Testcontainers), mocka serwera HTTP |
| end-to-end | całą ścieżkę od żądania do bazy | pełnego środowiska |

</div>
<div>

**Contract test portu** to jeden zestaw testów uruchamiany dla każdej implementacji danego portu, np. dla repozytorium JPA i dla repozytorium JDBC.

Gwarantuje, że wymiana adaptera nie zmieni zachowania widocznego dla aplikacji, nawet jeśli adaptery inaczej obsługują współbieżność.

</div>
</div>

<div class="takeaway">

Szybkie testy domeny ułatwiają bezpieczną refaktoryzację reguł, a testy adapterów sprawdzają integrację tam, gdzie rzeczywiście używa się technologii.

</div>

---

# Architektura jako kod

Reguły architektury zapisane tylko w dokumentacji z czasem przestają być przestrzegane. Dlatego zapisuje się je jako testy, które uruchamiają się przy każdym buildzie i blokują zmiany naruszające te reguły.

<div class="cols">
<div>

Typowe reguły:
- kierunek zależności między warstwami lub pierścieniami,
- brak zależności rdzenia aplikacji od frameworka i bibliotek persystencji,
- deterministyczna domena, która nie odczytuje bezpośrednio zegara systemowego,
- brak cykli między modułami,
- brak zależności między adapterami,
- konwencje nazewnicze i położenie klas w pakietach.

</div>
<div>

Narzędzia:
- **ArchUnit** sprawdza zależności między pakietami i klasami w zwykłych testach jednostkowych,
- **Spring Modulith** weryfikuje granice modułów i dozwolone zależności między nimi,
- **jMolecules** pozwala oznaczać pojęcia DDD (`@AggregateRoot`, `@ValueObject`) i sprawdzać związane z nimi reguły.

W istniejącym projekcie reguły wprowadza się stopniowo: dotychczasowe naruszenia się „zamraża” (`FreezingArchRule`), a nowe są blokowane.

</div>
</div>

---

# Dokumentowanie architektury: ADR i model C4

Na architekturę składa się nie tylko kod, ale też wiedza o tym, dlaczego wygląda ona tak, a nie inaczej, oraz jak wygląda całość. Obie te rzeczy dokumentuje się w lekkiej formie, przechowywanej razem z kodem.

<div class="cols">
<div class="card">

## Architecture Decision Record (ADR)

Krótki dokument opisujący jedną decyzję architektoniczną:
- **kontekst**: jaki problem rozwiązujemy i jakie czynniki trzeba uwzględnić,
- **decyzja**: co wybraliśmy,
- **konsekwencje**: co zyskujemy, a co tracimy.

ADR odpowiada na pytanie „dlaczego tak?”, zanim ktoś je zada. Nieaktualnych decyzji się nie usuwa, tylko zastępuje je nowymi.

</div>
<div class="card good">

## Model C4

Opisuje architekturę na czterech poziomach szczegółowości:
1. **Context**: system, jego użytkownicy i systemy zewnętrzne,
2. **Container**: aplikacje, bazy danych, brokery,
3. **Component**: moduły wewnątrz aplikacji,
4. **Code**: klasy (ten poziom zwykle się generuje).

Diagramy można opisać tekstowo (Structurizr DSL) albo wygenerować na podstawie modułów aplikacji.

</div>
</div>

---

# Od aplikacji do systemu: spektrum stylów

Te same zasady wyznaczania granic stosuje się w większej skali. Style systemów różnią się liczbą jednostek wdrożenia i tym, jak mocno egzekwowane są granice między częściami systemu.

| Styl | Jednostki wdrożenia | Granice modułów | Dane | Integracja |
|---|---|---|---|---|
| monolit | jedna | umowne albo żadne | wspólne | wywołania w procesie |
| modularny monolit | jedna | wymuszone w kodzie i testach | osobne dla każdego modułu | API modułu, zdarzenia |
| SOA | wiele | kontrakty usług | często współdzielone | ESB, standardy WS-* |
| mikroserwisy | wiele, wdrażanych niezależnie | proces i sieć | wyłącznie własne | API sieciowe, komunikaty |

- Jakość granic i liczba jednostek wdrożenia to dwie niezależne cechy. Monolit może być dobrze podzielony, a mikroserwisy źle.
- System rozproszony bez dobrych granic to **distributed monolith**: ponosi koszty sieci i wdrożeń, a nie daje niezależności.

---

# Monolit i modularny monolit

Monolit nie jest z założenia zły. Problemem jest monolit bez wewnętrznych granic, w którym wszystko zależy od wszystkiego.

<div class="cols">
<div class="card warm">

## Monolit

Aplikacja rozwijana, testowana i wdrażana jako jeden artefakt.

<ul class="pros">
<li>nowe funkcje, także pomocnicze, dodaje się łatwo</li>
<li>prosta infrastruktura, transakcje i debugowanie</li>
</ul>
<ul class="cons">
<li>od pewnej wielkości trudny w rozwoju i utrzymaniu</li>
<li>skalować można tylko całość</li>
<li>uzależnienie od jednej technologii</li>
</ul>

</div>
<div class="card good">

## Modularny monolit

Jeden artefakt złożony z modułów, które:
- są od siebie niezależne,
- mają jasno określoną odpowiedzialność, zwykle odpowiadającą jednemu kontekstowi,
- udostępniają publiczny kontrakt i ukrywają wnętrze,
- mogą zostać ponownie użyte albo w przyszłości wydzielone do osobnej usługi.

Daje większość korzyści z podziału (granice, niezależny rozwój modułów, testy) bez kosztów komunikacji sieciowej i rozproszonej spójności.

</div>
</div>

---

# Jak egzekwować granice modułu

Granica modułu jest tyle warta, ile mechanizm, który jej pilnuje. Kolejne mechanizmy zapewniają coraz silniejszą izolację, ale są też coraz droższe.

| Mechanizm | Co wymusza | Kiedy działa | Koszt |
|---|---|---|---|
| konwencja pakietów | nic, tylko dobrą wolę zespołu | nigdy | zerowy |
| modyfikatory dostępu (package-private, `sealed`) | ukrycie wnętrza, zamknięte hierarchie wyników | podczas kompilacji, ale nie obejmują podpakietów | niski |
| moduły Maven / Gradle | brak dostępu do klas spoza zadeklarowanych zależności | podczas kompilacji | konfiguracja buildu |
| JPMS (`module-info.java`) | `exports` dla kompilacji, `opens` dla refleksji | podczas kompilacji i działania | deskryptory modułów, problemy z refleksją |
| Spring Modulith | dozwolone zależności, komunikacja przez zdarzenia | w testach | niski |
| osobny proces | wyłącznie komunikacja przez sieć | zawsze | sieć, wdrożenia, spójność danych |

<div class="takeaway">

Moduły komunikują się przez publiczne API (fasadę) albo przez zdarzenia. Moduł, który korzysta z innego modułu, definiuje własny port i tłumaczy cudzy kontrakt na własny język.

</div>

---

# SOA i mikroserwisy

Oba style dzielą system na rozproszone usługi z publicznymi kontraktami. Różnią się tym, gdzie znajduje się logika integracji i czy usługi współdzielą dane.

<div class="cols">
<div>

**Architektura zorientowana na usługi (SOA)**
- usługi mają być wielokrotnie wykorzystywane w całej organizacji i często **współdzielą dane**,
- powstają i są wdrażane według wspólnych standardów organizacji,
- łączy je **ESB** (Enterprise Service Bus), który odpowiada za routing, transformację komunikatów i orkiestrację procesów,
- z czasem ESB staje się centralnym elementem, który trudno zmienić.

</div>
<div>

**Mikroserwisy**
- niewielkie usługi zbudowane wokół jednej **business capability**, zwykle jednego Bounded Contextu,
- **nie współdzielą danych**: każda ma własną bazę i komunikuje się z innymi wyłącznie przez API,
- **wdraża się je niezależnie** i to jest ich najważniejsza cecha,
- obowiązuje zasada *smart endpoints, dumb pipes*: logika jest w usługach, a infrastruktura komunikacyjna tylko przekazuje komunikaty.

</div>
</div>

<div class="takeaway">

O różnicy nie decyduje wielkość usług, lecz to, gdzie znajduje się logika integracji (w ESB czy w usługach) i czy dane są współdzielone.

</div>

---

# Mikroserwisy: zyski i koszty

Mikroserwisy nie usuwają złożoności. Przenoszą ją z kodu aplikacji do sieci, infrastruktury i organizacji pracy.

<div class="cols">
<div>

**Zalety**
<ul class="pros">
<li>modularność i możliwość ponownego użycia usług</li>
<li>skalowanie tylko tych części, które są wąskim gardłem</li>
<li>lepsze wykorzystanie zasobów</li>
<li>izolacja awarii i wysoka dostępność</li>
<li>małe, zrozumiałe bazy kodu i samodzielne zespoły</li>
</ul>

**Kwestie techniczne do rozwiązania:** konfiguracja, konteneryzacja, service discovery, dostęp do danych i transakcje, monitoring i tracing, bezpieczeństwo, CI/CD, integracja z frontendem.

</div>
<div>

**Wyzwania**
<ul class="cons">
<li>złożoność na poziomie całego systemu: infrastruktura, utrzymanie, funkcje pomocnicze w każdej usłudze</li>
<li>synchronizacja stanu i procesów bez wspólnej transakcji</li>
<li>koszty zespołu, sprzętu i utrzymania</li>
<li>niższa wydajność i większa podatność na awarie z powodu komunikacji sieciowej</li>
<li>kontrakty między usługami i ich wersjonowanie</li>
</ul>

</div>
</div>

---

# Jak podzielić system na usługi

Podział systemu na usługi trudno później zmienić. Usługi projektuje się, zaczynając od ich odpowiedzialności i interfejsu, a nie od istniejącego kodu czy tabel.

<div class="cols">
<div>

**Podział domenowy lub procesowy**
- według kontekstów i business capabilities, np. płatności, zamówienia, wysyłki,
- według procesu, np. składanie zamówienia od koszyka do potwierdzenia.

**Podział techniczny**
- według wymagań jakościowych, np. wyszukiwanie pełnotekstowe ma inne potrzeby niż przeglądanie kategorii,
- według obciążenia, wymagań bezpieczeństwa lub cyklu wydawniczego.

</div>
<div>

- Ostateczny podział zależy od wymagań systemu i istniejącej funkcjonalności.
- Granica usługi powinna pokrywać się z granicą kontekstu. Rozdzielenie agregatu między dwie usługi oznacza konieczność transakcji rozproszonej.
- Najpierw projektuje się **API** i odpowiedzialność usługi, a dopiero potem jej implementację.
- Migrację przeprowadza się stopniowo, np. według wzorca **Strangler Fig**: nowa usługa przejmuje kolejne fragmenty starego systemu, aż będzie można go wyłączyć.

</div>
</div>

---

# Makro- i mikroarchitektura (ISA)

Independent Systems Architecture (ISA) to zbiór zasad dla systemów złożonych z niezależnych modułów. Pomaga ustalić, które decyzje są wspólne dla wszystkich modułów, a które należą do każdego z nich.

<div class="cols">
<div>

**Dwa poziomy decyzji**
- **makroarchitektura** obejmuje wszystkie moduły: protokół komunikacji, bezpieczeństwo, format metadanych, sposób integracji,
- **mikroarchitektura** dotyczy jednego modułu: języka programowania, bazy danych, bibliotek,
- część decyzji ma charakter operacyjny: dostarczanie konfiguracji, monitoring, analiza logów, wdrażanie.

Moduły komunikują się wyłącznie przez kontrakt i działają jako osobne procesy, kontenery lub maszyny wirtualne.

</div>
<div>

**Standaryzacja i autonomia**
- liczba sposobów integracji jest ograniczona i ustandaryzowana,
- metadane przekazywane między modułami, np. identyfikatory korelacji, mają wspólny format,
- konfiguracja, monitoring, tracing i wdrażanie działają w każdym module tak samo,
- każdy moduł ma własny pipeline wdrożeniowy,
- moduł działa dalej, nawet gdy pozostałe są niedostępne.

</div>
</div>

<div class="takeaway">

Wybór protokołu komunikacji to typowa decyzja makroarchitektoniczna: dotyczy wszystkich usług jednocześnie, a jej późniejsza zmiana jest kosztowna.

</div>

---

# Wydzielenie usługi to wymiana adapterów

Jeśli granice i porty modułu zostały dobrze zaprojektowane, przeniesienie go do osobnej usługi nie wymaga zmian w logice biznesowej. Zmieniają się tylko adaptery, czyli sposób komunikacji.

| W modularnym monolicie | Po wydzieleniu usługi zamówień |
|---|---|
| wywołanie API modułu płatności w tym samym procesie | żądanie HTTP z retry i limitem współbieżności |
| zdarzenia aplikacyjne przekazywane w pamięci | te same zdarzenia wysyłane do Kafki przez outbox |
| rozbudowana hierarchia typów w kontrakcie | płaski komunikat JSON, zrozumiały w każdej technologii |
| jedna baza danych | dwie bazy, każda z własnym Event Publication Registry |
| domena i use case'y zamówień | **bez zmian** |

<div class="takeaway">

Gdy moduł staje się usługą, wywołanie metody zostaje zastąpione komunikacją sieciową. Pojawiają się pytania, których wcześniej nie było: jakiego formatu danych użyć, jak opisać kontrakt, jak zgłaszać błędy, jak wersjonować API i co zrobić, gdy sieć zawiedzie.

</div>

---

# Logika zdalna: co zmienia sieć

Logika zdalna to logika udostępniana przez sieć w ramach systemu rozproszonego. Wywołanie metody w obrębie procesu jest szybkie, niezawodne i sprawdzane przez kompilator. Wywołanie przez sieć nie ma żadnej z tych cech.

<div class="cols">
<div>

Komunikacja sieciowa wprowadza:
- **opóźnienia**, które dodatkowo bywają zmienne,
- **częściowe awarie**: żądanie dotarło, ale odpowiedź zginęła, więc nie wiadomo, czy operacja została wykonana,
- **serializację**, bo dane muszą mieć format niezależny od języka programowania,
- **kontrakt**, który obie strony zmieniają w różnym czasie,
- **zagadnienia bezpieczeństwa** na granicy zaufania.

</div>
<div class="card bad">

## Fallacies of distributed computing

Lista błędnych założeń, którą sformułowali inżynierowie Sun Microsystems (m.in. L. Peter Deutsch). Projektanci systemów rozproszonych często przyjmują je nieświadomie:

1. sieć jest niezawodna,
2. opóźnienie jest zerowe,
3. przepustowość jest nieskończona,
4. sieć jest bezpieczna,
5. topologia sieci się nie zmienia,
6. siecią zarządza jeden administrator,
7. transport danych nic nie kosztuje,
8. sieć jest jednorodna.

</div>
</div>

---

# Krótka historia logiki zdalnej

Kolejne technologie komunikacji powstawały w odpowiedzi na ograniczenia poprzednich. Ta historia wyjaśnia, dlaczego dziś w jednym systemie używa się kilku stylów jednocześnie.

<div class="chain">
<div class="box soft">RPC, CORBA<span class="sub">lata 80. i 90.</span></div><div class="arr">→</div>
<div class="box soft">Java RMI, EJB<span class="sub">koniec lat 90.</span></div><div class="arr">→</div>
<div class="box">SOAP, WSDL<span class="sub">1998–2001</span></div><div class="arr">→</div>
<div class="box">REST<span class="sub">2000</span></div><div class="arr">→</div>
<div class="box core">GraphQL, gRPC<span class="sub">2015</span></div>
</div>

<div class="cols">
<div class="card">

## Obiekty zdalne

Komponent biznesowy, np. Stateless Session Bean w EJB, jest dostępny przez Java RMI. Klient wyszukuje go w JNDI i otrzymuje wygenerowane proxy, które wygląda jak obiekt lokalny.

Wniosek: ***location transparency* jest złudzeniem**. Jeśli wywołanie zdalne wygląda jak lokalne, łatwo zaprojektować interfejs wymagający wielu wywołań (*chatty*) i zapomnieć o awariach sieci.

</div>
<div class="card good">

## Kolejne rozwiązania

- SOAP: niezależność od platformy i języka dzięki XML.
- REST: prostota i skalowalność wzorowane na sieci WWW.
- GraphQL: odpowiedź dopasowana do potrzeb klienta.
- gRPC: wydajność i streaming w komunikacji między usługami.

Równolegle rozwijał się model **asynchroniczny**, oparty na brokerach komunikatów (JMS, później Kafka).

</div>
</div>

---

# Cechy komunikacji

Protokół wybiera się dla konkretnej granicy: między przeglądarką a backendem, między usługami albo między systemem a partnerem zewnętrznym. Wybór ułatwia rozpatrzenie kilku niezależnych cech komunikacji.

| Cecha | Warianty | Od czego zależy wybór |
|---|---|---|
| czas | **synchroniczna** (nadawca czeka na odpowiedź) albo **asynchroniczna** (nadawca nie czeka) | czy wynik jest potrzebny od razu i czy awaria odbiorcy może blokować nadawcę |
| format | **tekstowy** (XML, JSON) albo **binarny** (Protobuf, Avro) | czytelność i narzędzia kontra rozmiar i szybkość |
| kontrakt | schemat **obowiązkowy** albo **opcjonalny** | bezpieczeństwo typów kontra elastyczność |
| liczba odbiorców | jeden (request–response, queue) albo wielu (publish–subscribe) | czy nadawca wie, kto ma zareagować |
| styl | **operacje**, **zasoby**, **zapytania** albo **messaging** | jak klient wyraża swoją intencję |

<div class="takeaway">

Systemy zwykle łączą kilka wariantów: synchroniczne API dla użytkowników, asynchroniczne komunikaty między usługami i binarny protokół tam, gdzie liczy się wydajność.

</div>

---

# Cztery style interakcji

Styl interakcji określa, w jaki sposób klient wyraża swoją intencję wobec usługi. Od stylu zależy kształt kontraktu, sposób zgłaszania błędów i możliwość cache'owania odpowiedzi.

<div class="cols-4">
<div class="card">

## Operacje (RPC)

Klient wywołuje nazwaną procedurę z parametrami, np. „dodaj transakcję do karty”.

Interfejs przypomina zestaw metod, a kontrakt opisuje operacje i typy danych.

**SOAP, gRPC**

</div>
<div class="card good">

## Zasoby

Klient zmienia lub odczytuje stan zasobu za pomocą stałego zestawu metod, np. tworzy transakcję w kolekcji transakcji karty.

**REST**

</div>
<div class="card warm">

## Zapytania

Klient opisuje, jakie dane chce otrzymać, np. saldo karty i trzy ostatnie transakcje, a serwer zwraca dokładnie te dane.

**GraphQL**

</div>
<div class="card bad">

## Messaging

Nadawca publikuje polecenie lub informację o zdarzeniu i nie czeka na odpowiedź. Odbiorca przetwarza komunikat, gdy jest gotowy.

**JMS, Kafka, AMQP**

</div>
</div>

<div class="takeaway">

Style się nie wykluczają. Często REST lub GraphQL obsługuje klientów zewnętrznych, a gRPC lub messaging służy do komunikacji między usługami.

</div>

---

# Kontrakt usługi sieciowej

Kontrakt sieciowy to opis, dzięki któremu dwie niezależne strony mogą się porozumieć bez znajomości swoich implementacji. Powinien być na tyle precyzyjny, żeby dało się na jego podstawie wygenerować kod albo sprawdzić zgodność implementacji.

<div class="cols">
<div>

Kontrakt opisuje:
- **operacje** lub zasoby udostępniane przez usługę,
- **typy danych** wejściowych i wyjściowych,
- **błędy** i ich znaczenie,
- **sposób dostępu**: transport, adres, zabezpieczenia.

| Styl | Opis kontraktu |
|---|---|
| SOAP | WSDL i XML Schema |
| REST | OpenAPI (dawniej WADL) |
| GraphQL | schemat w języku SDL |
| gRPC | plik `.proto` |
| messaging | AsyncAPI, Schema Registry |

</div>
<div>

<div class="card">

## Code-first

Najpierw powstaje kod, a kontrakt jest z niego **generowany**. Pozwala to szybko zacząć, ale łatwo przypadkiem zmienić kontrakt, np. zmieniając nazwę pola w klasie.

</div>

<div class="card good mt">

## Contract-first

Najpierw powstaje kontrakt, a kod (często generowany) jest do niego dopasowywany. Wymaga to dyscypliny, ale kontrakt jest wynikiem świadomej decyzji, a obie strony mogą pracować równolegle.

</div>

</div>
</div>

---

# Formaty danych i data binding

Dane przesyłane przez sieć trzeba zamienić z obiektów na tekst lub bajty, a po drugiej stronie z powrotem na obiekty. Format wpływa na czytelność, rozmiar komunikatów, szybkość przetwarzania i sposób wprowadzania zmian w kontrakcie.

| | XML | JSON | Protobuf |
|---|---|---|---|
| charakter | tekstowy, obszerny, z przestrzeniami nazw | tekstowy, zwięzły, oparty na składni obiektów JavaScript | binarny, zwarty |
| schemat | XML Schema (XSD) | JSON Schema, OpenAPI (opcjonalnie) | plik `.proto` (obowiązkowo) |
| data binding w Javie | JAXB, sterowany adnotacjami | Jackson, JSON-B | klasy generowane z kontraktu |
| zalety | walidacja, podpisy cyfrowe, dojrzałe standardy | popularność, czytelność, prostota | mały rozmiar, szybkość, kontrolowana ewolucja |
| typowe użycie | SOAP, integracje korporacyjne | REST, GraphQL, messaging | gRPC, messaging o dużej przepustowości |

- **Data binding**, czyli serializacja i deserializacja, należy do adaptera. Klasy z adnotacjami mapowania to DTO protokołu, a nie model domeny.
- Gdy typ nie pasuje do domyślnego mapowania (brak konstruktora bezargumentowego, Value Objects), stosuje się **adaptery typów**, np. `XmlAdapter` w JAXB.

---

# Protokół to adapter

Wszystko, co powiedzieliśmy o portach i adapterach, dotyczy także komunikacji sieciowej. Każdy protokół to osobny adapter tego samego use case'u.

<div class="chain">
<div class="stackcol">
<div class="box">kontroler REST</div>
<div class="box">endpoint SOAP</div>
<div class="box">usługa gRPC</div>
<div class="box">resolver GraphQL</div>
<div class="box">listener komunikatów</div>
</div>
<div class="arr">→</div>
<div class="box soft">komenda<span class="sub">„dodaj transakcję”</span></div>
<div class="arr">→</div>
<div class="box core">port wejściowy<span class="sub"><code>AddTransactionUseCase</code></span></div>
<div class="arr">→</div>
<div class="box core">domena</div>
</div>

- Adapter tłumaczy **format** (DTO na komendę), **język** (`IN`/`OUT` w API na wpływ i płatność w domenie) i **błędy** (wyjątek domenowy na kod HTTP, SOAP Fault albo status gRPC).
- Po stronie wyjściowej jest tak samo: klient HTTP, stub gRPC czy producent komunikatów są ukryte za portem aplikacji.
- Nowy protokół oznacza nowy adapter, a nie zmianę logiki biznesowej.

<div class="takeaway">

Przy każdym protokole warto zapytać: jak wygląda kontrakt, jak zgłasza się błędy i jak wprowadza się zmiany?

</div>

---

# Web services: założenia i standardy

Web services to funkcjonalność udostępniana przez sieć wraz z zestawem standardów, które pozwalają ją opisać, wyszukać i wywołać niezależnie od platformy.

<div class="cols">
<div>

**Założenia**
- komunikacja w środowisku **heterogenicznym**: różne języki, systemy i producenci oprogramowania,
- niezależność od platformy sprzętowej, programowej i transportowej,
- modularność i możliwość wielokrotnego użycia usług.

**Typowy scenariusz**
1. dostawca publikuje opis usługi (WSDL) w rejestrze,
2. konsument wyszukuje usługę i pobiera jej opis,
3. konsument wysyła żądanie w formacie SOAP,
4. dostawca przetwarza żądanie i odsyła odpowiedź SOAP.

</div>
<div>

<div class="stack">
<div class="dark">wyszukiwanie: rejestr UDDI</div>
<div>opis i kontrakt: WSDL i XML Schema</div>
<div>komunikacja: SOAP</div>
<div class="hot">transport: HTTP/HTTPS, JMS, SMTP</div>
</div>

<p class="small mt">Rejestry UDDI nie przyjęły się w praktyce. Plik WSDL przekazuje się zwykle bezpośrednio albo udostępnia pod adresem usługi.</p>

</div>
</div>

---

# WSDL: kontrakt usługi w XML

Dokument WSDL jest kontraktem między dostawcą a konsumentem usługi. Opisuje operacje oraz wszystko, co jest potrzebne do ich wywołania: dane wejściowe, wyniki, błędy i sposób dostępu, bez ujawniania szczegółów implementacji.

<div class="cols-wide">
<div>

| Element (WSDL 1.1) | Co opisuje |
|---|---|
| `definitions` | element główny i przestrzenie nazw |
| `types` | typy danych zdefiniowane w XML Schema |
| `message` | komunikaty przesyłane w ramach usługi |
| `portType` | operacje usługi, czyli jej interfejs |
| `binding` | protokół i kodowanie użyte do wywołania operacji |
| `service` / `port` | usługę i adres, pod którym jest dostępna |

</div>
<div class="card">

## Część abstrakcyjna i konkretna

Elementy `types`, `message` i `portType` określają, **co** usługa robi.

Elementy `binding` i `service` określają, **jak i gdzie** można ją wywołać.

Ten sam interfejs można więc udostępnić przez różne transporty. To podział na port i adapter, zapisany w XML.

<p class="small">W WSDL 2.0 zmieniono nazwy: <code>portType</code> na <code>interface</code>, a <code>port</code> na <code>endpoint</code>.</p>

</div>
</div>

---

# Komunikat SOAP

SOAP to protokół, który określa format wiadomości wymienianych między dostawcą a konsumentem usługi. Wiadomości zapisuje się w XML i każda ma taką samą strukturę.

<div class="cols">
<div>

```xml
<soap:Envelope
    xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
    xmlns:p="urn:example:payments:v1">
  <soap:Header>
    <p:CorrelationId>9c1e…</p:CorrelationId>
  </soap:Header>
  <soap:Body>
    <p:AddTransaction>
      <p:cardNumber>1111222233334444</p:cardNumber>
      <p:amount currency="PLN">100.00</p:amount>
      <p:type>PAYMENT</p:type>
    </p:AddTransaction>
  </soap:Body>
</soap:Envelope>
```

</div>
<div>

- **Envelope** to element główny, który określa wersję protokołu.
- **Header** zawiera opcjonalne metadane: dane zabezpieczeń, identyfikator korelacji, informacje o transakcji lub routingu.
- **Body** zawiera właściwą treść: wywołanie operacji albo jej wynik.
- **Fault** to standardowy sposób zgłaszania błędu. Wskazuje, czy błąd leży po stronie nadawcy, czy odbiorcy, i zawiera szczegóły.

SOAP jest protokołem **operacji**. Nazwa wywoływanej operacji znajduje się w treści wiadomości, a HTTP służy wyłącznie do jej przesłania (zwykle metodą POST na jeden adres).

</div>
</div>

---

# Pośrednicy, rozszerzenia i dane binarne

Wiadomość SOAP zawiera wszystkie potrzebne informacje, więc w drodze do odbiorcy może przechodzić przez pośredników. Jest to źródłem zarówno siły SOAP w środowiskach korporacyjnych, jak i jego złożoności.

<div class="cols">
<div>

**Pośrednicy i handlery**
- nadawca i odbiorca nie są ze sobą bezpośrednio powiązani,
- po drodze wiadomość może przetworzyć dowolna liczba pośredników, którzy odczytują i modyfikują nagłówki,
- pośrednicy weryfikują podpisy i uprawnienia, eliminują powtórzone żądania, pilnują kolejności i prowadzą audyt,
- łańcuch handlerów działa podobnie jak aspekty, tylko na poziomie wiadomości.

Rozszerzenia z rodziny WS-*: **WS-Security**, WS-Addressing, WS-ReliableMessaging, WS-AtomicTransaction.

</div>
<div>

**Przesyłanie danych binarnych** (obrazów, dokumentów, nagrań)

| Sposób | Cechy |
|---|---|
| base64 w treści | proste, ale zwiększa rozmiar o około jedną trzecią i wymaga kodowania po obu stronach; nadaje się tylko do małych danych |
| SwA | załączniki MIME przesyłane razem z wiadomością |
| DIME | format opracowany przez Microsoft, dziś nieużywany |
| **MTOM / XOP** | dane binarne poza dokumentem XML, w treści tylko odnośnik; obecny standard |

</div>
</div>

---

# SOAP w Javie: JAX-WS

JAX-WS (Java API for XML Web Services, obecnie Jakarta XML Web Services) zastąpiło starsze JAX-RPC. Pozwala tworzyć i wywoływać usługi SOAP tak, jakby były zwykłymi obiektami Javy.

<div class="cols">
<div>

- Klient i usługa korzystają z obiektów pośredniczących generowanych na podstawie WSDL: **stub** po stronie klienta i **tie** po stronie usługi.
- **JAX-WS runtime** zamienia wywołania metod na komunikaty SOAP i odwrotnie.
- Treść komunikatów mapuje na obiekty **JAXB**. Dane binarne można przesyłać przez **MTOM** i określić próg rozmiaru, od którego trafiają do załącznika.
- JAX-WS obsługuje oba podejścia: **code-first** (z adnotacji powstaje WSDL) i **contract-first** (z WSDL powstają klasy).

</div>
<div>

| Adnotacja | Element WSDL |
|---|---|
| `@WebService` | `portType`, `service`, `port` |
| `@WebMethod` | operacja |
| `@WebParam`, `@WebResult` | części komunikatów |
| `@Oneway` | operacja bez odpowiedzi |

<div class="grid3">
<div class="box soft">klient</div><div></div><div class="box soft">usługa</div>
<div class="box">stub</div><div></div><div class="box">tie</div>
<div class="box dash">JAX-WS runtime</div><div class="arr">⇄ SOAP ⇄</div><div class="box dash">JAX-WS runtime</div>
</div>

</div>
</div>

---

# REST: styl architektoniczny, a nie protokół

REST (Representational State Transfer) opisał Roy Fielding w rozprawie doktorskiej z 2000 roku. Wyjaśnił w niej, dlaczego sieć WWW tak dobrze się skaluje, i sformułował zestaw ograniczeń (constraints), które można zastosować przy projektowaniu API.

| Ograniczenie | Na czym polega i co daje |
|---|---|
| **client–server** | klient nie zna sposobu przechowywania danych ani logiki serwera, a serwer nie zna sposobu prezentacji; obie strony rozwijają się niezależnie |
| **stateless** | każde żądanie zawiera wszystkie informacje potrzebne do jego obsługi, więc może je obsłużyć dowolna instancja serwera |
| **cacheable** | odpowiedź określa, czy i jak długo można ją przechowywać, co zmniejsza obciążenie serwera |
| **uniform interface** | wszystkie zasoby obsługuje się w ten sam sposób, co upraszcza komunikację i zmniejsza powiązania |
| **layered system** | pośrednicy (proxy, gateway, CDN) są niewidoczni dla klienta |
| code on demand (opcjonalne) | serwer może przesłać klientowi kod do wykonania |

<div class="takeaway">

REST nie wymaga konkretnego protokołu, ale w praktyce prawie zawsze korzysta z HTTP lub HTTPS.

</div>

---

# Zasoby i uniform interface

Podstawowym pojęciem w REST jest zasób, czyli wszystko, co warto nazwać i udostępnić: karta, transakcja, zamówienie. Klient nie wywołuje operacji, tylko odczytuje i zmienia stan zasobów za pomocą stałego zestawu metod.

<div class="cols">
<div>

**Zasób**
- ma unikalny identyfikator, czyli **URI**,
- jest niezależny od **reprezentacji**: tę samą kartę można przesłać jako JSON, XML lub YAML,
- jego stan zmienia się wyłącznie za pomocą predefiniowanych metod.

**Elementy uniform interface**
1. identyfikacja zasobów za pomocą URI,
2. zmiana zasobów przez przesyłanie ich reprezentacji,
3. komunikaty, które same się opisują,
4. hipermedia jako mechanizm sterowania stanem aplikacji (HATEOAS).

</div>
<div>

**Use case'y wyrażone jako zasoby**

| Intencja | Żądanie |
|---|---|
| wydaj kartę | `POST /cards` |
| dodaj transakcję | `POST /cards/{n}/transactions` |
| pokaż kartę | `GET /cards/{n}` |
| historia transakcji | `GET /cards/{n}/transactions?page=0` |
| anuluj zamówienie | `POST /orders/{id}/cancellation` |

<p class="small">Nie każda intencja to operacja CRUD. Proces, np. anulowanie zamówienia, również może być zasobem z własnym stanem.</p>

</div>
</div>

---

# HATEOAS i Richardson Maturity Model

HATEOAS (Hypermedia as the Engine of Application State) oznacza, że odpowiedź zawiera nie tylko stan zasobu, ale też odnośniki do możliwych dalszych działań. Klient porusza się po API podobnie jak użytkownik po stronach WWW.

<div class="cols">
<div>

- Klient przekazuje dane w treści żądania, parametrach, nagłówkach i URI.
- Serwer zwraca stan zasobu w treści, kodzie odpowiedzi i nagłówkach, a do tego **linki** do dostępnych akcji, np. „zapłać” albo „anuluj”.
- Klient nie musi znać reguł biznesowych, np. tego, kiedy zamówienie można opłacić. Wystarczy, że sprawdzi, czy odpowiedź zawiera odpowiedni link.

</div>
<div>

**Richardson Maturity Model**

| Poziom | Cecha API |
|---|---|
| 0 | jeden adres i jedna metoda, czyli zdalne wywołania przez HTTP, jak w SOAP |
| 1 | osobne zasoby z własnymi URI |
| 2 | metody i kody statusu HTTP używane zgodnie z przeznaczeniem |
| 3 | hipermedia określają dostępne przejścia między stanami |

<p class="small">Większość API określanych jako REST w praktyce kończy na poziomie 2.</p>

</div>
</div>

---

# Bezstanowość i cache

Te dwa ograniczenia REST mają największy wpływ na skalowalność. Razem pozwalają obsłużyć duży ruch przy stosunkowo prostej infrastrukturze.

<div class="cols">
<div>

**Bezstanowość**
- serwer nie przechowuje stanu sesji w pamięci; stan jest po stronie klienta albo zapisany w zasobach,
- każde żądanie zawiera wszystko, co jest potrzebne do jego obsługi,
- instancje serwera są wymienne, więc skalowanie poziome polega na dodaniu kolejnych instancji,
- dane uwierzytelniające, np. token, są przesyłane z każdym żądaniem.

</div>
<div>

**Cache i żądania warunkowe**
- nagłówek `Cache-Control` określa, czy i jak długo można przechowywać odpowiedź,
- `ETag` oznacza wersję reprezentacji. Klient przesyła ją w nagłówku `If-None-Match`, a jeśli zasób się nie zmienił, dostaje `304 Not Modified` bez treści,
- `If-Match` przy zapisie realizuje **optimistic locking** na poziomie HTTP: jeśli wersja się zmieniła, serwer odpowiada `412 Precondition Failed`.

</div>
</div>

<div class="takeaway">

`ETag` może odpowiadać numerowi wersji agregatu w bazie danych. Konflikt równoległych zmian staje się wtedy jawną częścią kontraktu API.

</div>

---

# Protokół HTTP

HTTP to synchroniczny i bezstanowy protokół typu request–response. Klient i serwer wymieniają komunikaty, które w wersji HTTP/1.1 mają postać tekstową.

<div class="cols">
<div>

```http
POST /api/cards/1111222233334444/transactions HTTP/1.1
Host: payments.example
Content-Type: application/json
Accept: application/json, application/problem+json

{"type": "OUT", "amount": 100, "currencyCode": "PLN"}
```

```http
HTTP/1.1 201 Created
Location: /api/cards/1111222233334444/transactions/7f3c…
Content-Type: application/json

{"id": "7f3c…"}
```

</div>
<div>

Każdy komunikat składa się z trzech części:
- **linii początkowej**: w żądaniu jest to metoda i URI, a w odpowiedzi kod statusu z opisem,
- **nagłówków**, czyli metadanych opisujących treść i komunikację (typ, rozmiar, kodowanie, język, cache, autoryzacja),
- **treści** oddzielonej od nagłówków pustą linią.

HTTP/2 i HTTP/3 zachowują tę samą semantykę (metody, nagłówki, kody statusu), ale zmieniają sposób przesyłania danych: używają binarnych ramek i wielu równoległych strumieni w jednym połączeniu.

</div>
</div>

---

# Metody HTTP

Metoda określa, co klient chce zrobić z zasobem. Dla niezawodności komunikacji najważniejsze są dwie cechy metod: bezpieczeństwo i idempotentność.

| Metoda | Przeznaczenie | Bezpieczna | Idempotentna |
|---|---|---|---|
| `GET` | pobranie reprezentacji zasobu | tak | tak |
| `HEAD` | jak `GET`, ale bez treści odpowiedzi (np. sprawdzenie, czy zasób istnieje) | tak | tak |
| `OPTIONS` | sprawdzenie, jakie metody i opcje obsługuje zasób | tak | tak |
| `PUT` | zapisanie pełnej reprezentacji pod wskazanym URI, czyli utworzenie lub zastąpienie zasobu | nie | tak |
| `DELETE` | usunięcie zasobu | nie | tak |
| `POST` | przetworzenie danych przez zasób, np. utworzenie zasobu podrzędnego | nie | **nie** |
| `PATCH` | zmiana części zasobu | nie | **nie** (nie jest to gwarantowane) |

- Metoda **bezpieczna** nie zmienia stanu serwera. Metoda **idempotentna** daje ten sam efekt niezależnie od tego, ile razy zostanie wykonana.
- Od idempotentności zależy, czy klient może bezpiecznie ponowić żądanie po timeoucie. W przypadku `POST` stosuje się **klucz idempotentności** (nagłówek `Idempotency-Key`), żeby ponowna próba nie utworzyła drugiej płatności.

---

# Kody statusu jako część kontraktu

Kod statusu informuje klienta o wyniku i podpowiada, jak zareagować: wyświetlić wynik, poprawić żądanie, poinformować użytkownika albo spróbować ponownie.

<div class="cols">
<div>

| Kod | Znaczenie |
|---|---|
| `200 OK` | sukces, odpowiedź zawiera wynik |
| `201 Created` | utworzono zasób, jego adres jest w nagłówku `Location` |
| `202 Accepted` | żądanie przyjęto do przetworzenia asynchronicznego |
| `204 No Content` | sukces, odpowiedź nie ma treści |
| `304 Not Modified` | kopia w cache klienta jest aktualna |

</div>
<div>

| Kod | Znaczenie |
|---|---|
| `400 Bad Request` | żądanie ma niepoprawny format |
| `404 Not Found` | zasób nie istnieje |
| `406` / `415` | serwer nie obsługuje żądanego formatu odpowiedzi / formatu żądania |
| `409` / `412` | konflikt stanu lub wersji zasobu |
| `422 Unprocessable Content` | żądanie jest poprawne, ale narusza regułę biznesową |
| `503` / `504` | zależność jest niedostępna / przekroczono czas oczekiwania |

</div>
</div>

<div class="takeaway">

Trzy poziomy walidacji przekładają się bezpośrednio na kody statusu: błąd formatu to 400, brak agregatu to 404, a naruszenie reguły biznesowej to 422. O wyborze kodu decyduje adapter, a nie domena.

</div>

---

# Content negotiation i Problem Details

Jedna usługa obsługuje klientów, którzy oczekują różnych formatów i języków. HTTP pozwala to uzgodnić, a dla odpowiedzi z błędami istnieje standardowy format.

<div class="cols">
<div>

**Content negotiation**

| Nagłówek | Co uzgadnia |
|---|---|
| `Accept`, `Content-Type` | format treści: JSON, XML, CSV |
| `Accept-Language` | język, np. komunikatów o błędach |
| `Accept-Encoding` | kompresję: gzip, br |

Serwer wybiera format preferowany przez klienta. Jeśli nie obsługuje żadnego z nich, odpowiada `406 Not Acceptable`.

</div>
<div>

**Problem Details (RFC 9457)**

```http
HTTP/1.1 422 Unprocessable Content
Content-Type: application/problem+json
Content-Language: pl

{
  "type": "https://payments.example/problems/insufficient-funds",
  "title": "Brak wystarczających środków",
  "status": 422,
  "detail": "Saldo karty jest niższe niż kwota płatności"
}
```

Wspólny format błędów pozwala klientom obsługiwać je jednakowo we wszystkich usługach.

</div>
</div>

---

# Dobre praktyki projektowania REST

Ograniczenia Fieldinga definiują, czym jest REST. Praktyka dodała do nich konwencje, dzięki którym API jest przewidywalne i łatwe w użyciu.

<div class="cols">
<div>

**Zasoby i adresy**
- identyfikatory są krótkie, zrozumiałe i osadzone w kontekście,
- składają się z **rzeczowników**, bo rodzaj operacji określa metoda HTTP,
- wskazują kolekcję albo jej element; nie miesza się liczby pojedynczej i mnogiej,
- relacje między zasobami wyraża się jako **podzasoby** (sub-resources),
- filtrowanie, sortowanie, paginację i grupowanie obsługuje się **parametrami zapytania** (query params),
- zasoby mają odpowiednią **granularność**: nie są ani jednym „workiem na wszystko”, ani setkami drobnych elementów.

</div>
<div>

**Komunikacja**
- metody HTTP są używane zgodnie z przeznaczeniem, a kody statusu odpowiadają wynikowi,
- format danych jest jawnie podany w nagłówkach, a domyślnym formatem jest JSON,
- odpowiedź z błędem zawiera właściwy kod statusu oraz treść z kodem błędu i opisem,
- poprawne odpowiedzi na `GET` można cache'ować,
- API **wersjonuje się od pierwszego dnia**,
- HATEOAS stosuje się tam, gdzie klient powinien sam odkrywać dostępne akcje.

<p class="small">Jeśli infrastruktura przepuszcza tylko metody <code>GET</code> i <code>POST</code>, stosuje się nagłówek <code>X-HTTP-Method-Override</code>.</p>

</div>
</div>

---

# REST w Javie i porównanie z SOAP

W Javie usługi REST tworzy się deklaratywnie: adnotacje łączą metody zwykłych klas z adresami i metodami HTTP, a resztą zajmuje się framework.

<div class="cols">
<div>

- **JAX-RS** (Jakarta RESTful Web Services) wiąże metody z URI, wstrzykuje dane żądania (parametry ścieżki, parametry zapytania, nagłówki), konwertuje treść na obiekty, mapuje wyjątki na odpowiedzi (`ExceptionMapper`) i obsługuje content negotiation. Najpopularniejsze implementacje to Jersey i RESTEasy.
- **Spring MVC** i Spring WebFlux oferują podobny model w ekosystemie Springa.
- Do opisu API służył kiedyś **WADL** (dialekt XML opisujący zasoby i relacje). Dziś używa się **OpenAPI**, generowanego z kodu albo tworzonego przed implementacją.

</div>
<div>

| | SOAP | REST |
|---|---|---|
| model | operacje | zasoby |
| kontrakt | formalny, obowiązkowy | opcjonalny |
| format | XML | dowolny, zwykle JSON |
| stan | możliwa komunikacja stanowa | bezstanowy |
| cache HTTP | nie | tak |
| mocne strony | obsługa starszych systemów, formalny kontrakt, standardy WS-*, narzędzia | prostota, skalowalność, wydajność, różne formaty, popularność |

</div>
</div>

---

# GraphQL: klient decyduje o kształcie odpowiedzi

GraphQL powstał w firmie Facebook w 2012 roku, a w 2015 roku został udostępniony publicznie. Był odpowiedzią na ograniczenia API opartych na zasobach w aplikacjach z wieloma ekranami i różnymi klientami. W GraphQL to klient określa, jakie dane chce otrzymać.

<div class="cols">
<div>

**Problemy, które rozwiązuje**
- **over-fetching**: endpoint zwraca więcej pól, niż potrzebuje dany ekran,
- **under-fetching**: jeden ekran wymaga kilku wywołań, np. najpierw karty, potem transakcji, a na końcu kursów walut,
- **wielu klientów** (web, aplikacje mobilne, partnerzy) o różnych potrzebach korzysta z jednego backendu.

</div>
<div>

**Założenia**
- jeden endpoint obsługuje wszystkie operacje,
- serwer udostępnia spójny **schemat** opisujący typy i relacje między nimi,
- klient wskazuje pola, a serwer zwraca dokładnie te pola,
- powiązane dane pobiera się w **jednym** wywołaniu.

</div>
</div>

<div class="chain">
<div class="box soft">klient<span class="sub">karta, saldo i trzy ostatnie transakcje</span></div>
<div class="arr">→</div>
<div class="box core">serwer GraphQL<span class="sub">jedno żądanie</span></div>
<div class="arr">→</div>
<div class="stackcol"><div class="box">model odczytu kart</div><div class="box">kursy walut</div></div>
</div>

---

# Schemat GraphQL jako kontrakt

Schemat opisuje wszystkie typy, pola i relacje oraz definiuje dostępne operacje: zapytania, mutacje i subskrypcje. Jest kontraktem między klientem a serwerem.

<div class="cols">
<div>

```graphql
type Card {
  number: ID!
  balance: Money!
  transactions(last: Int = 10): [Transaction!]!
}

type Query { card(number: ID!): Card }

type Mutation {
  addTransaction(input: AddTransactionInput!): AddTransactionResult!
}

union AddTransactionResult = TransactionAdded | InsufficientFunds

type Subscription { transactionRegistered(card: ID!): Transaction! }
```

</div>
<div>

- **System typów** obejmuje typy skalarne (także własne, np. kwotę), typy obiektowe, enumy, listy, typy non-null (`!`), typy wejściowe (input), interfejsy i unie (union).
- Dzięki typom schemat precyzyjnie opisuje strukturę domeny, a błędy w zapytaniach są wykrywane, zanim zapytanie zostanie wykonane.
- Serwer **sprawdza każde zapytanie względem schematu** przed jego realizacją.
- Unia wyników pozwala opisać błąd biznesowy, np. brak środków, jako zwykły element kontraktu, a nie wyjątek.

</div>
</div>

---

# Query, Mutation i Subscription

GraphQL rozróżnia trzy rodzaje operacji. Podział na odczyt i zapis jest jawnie zapisany w schemacie, podobnie jak w zasadzie CQS.

<div class="cols-3">
<div class="card">

## Query

Odczyt danych. Klient wskazuje potrzebne pola, a serwer zwraca tylko je, co ogranicza ilość przesyłanych danych.

```graphql
{ card(number: "1111…") {
    balance { amount currency }
    transactions(last: 3) {
      amount type } } }
```

</div>
<div class="card warm">

## Mutation

Tworzenie, zmiana i usuwanie danych, czyli operacje, które zmieniają stan systemu.

Mutacja może zwrócić zmienione dane, dzięki czemu klient od razu zna nowy stan.

Za poprawność i spójność zmiany odpowiada serwer, a konkretnie use case i model domeny.

</div>
<div class="card good">

## Subscription

Serwer **wysyła** dane do klienta, gdy wystąpi określone zdarzenie. Zwykle odbywa się to przez WebSocket.

Klient nie musi cyklicznie odpytywać serwera (polling), a aplikacja może na bieżąco reagować na zmiany.

Subskrypcje dobrze współpracują z Domain Events.

</div>
</div>

---

# GraphQL po stronie serwera

Z zewnątrz GraphQL to jeden endpoint, ale wewnątrz każde pole schematu ma własną funkcję, która dostarcza jego wartość. Taki model dobrze pasuje do architektury portów i adapterów.

<div class="cols">
<div>

- Funkcja dostarczająca wartość pola to **resolver**.
- Resolver mutacji jest **adapterem wejściowym**: zamienia dane wejściowe na komendę i wywołuje port use case'u.
- Resolvery zapytań mogą korzystać z **read modelu**, czyli modelu przygotowanego specjalnie do odczytu.
- **Introspekcja** pozwala klientowi pobrać opis schematu. Na jej podstawie działają generatory dokumentacji, eksploratory API, generatory typów i podpowiedzi w IDE.
- **Federacja** pozwala połączyć schematy wielu usług w jeden graf.

</div>
<div class="card bad">

## Problem N+1 przenosi się na serwer

Klient wysyła jedno żądanie, ale naiwnie napisane resolvery wykonują jedno zapytanie do bazy o listę kart i osobne zapytanie o transakcje każdej karty.

Rozwiązaniem jest **batching**: resolvery zbierają potrzebne identyfikatory, a dane są pobierane jednym zapytaniem i zapamiętywane na czas obsługi żądania (wzorzec DataLoader).

</div>
</div>

---

# GraphQL: zalety i koszty

GraphQL rozwiązuje rzeczywiste problemy API opartych na zasobach, ale zwiększa złożoność po stronie serwera i utrudnia niektóre rzeczy, które w REST są proste.

<div class="cols">
<div>

**Zalety w porównaniu z REST**
<ul class="pros">
<li>jeden endpoint zamiast wielu</li>
<li>powiązane dane w jednym wywołaniu, bez wielu round-tripów do serwera</li>
<li>silnie typowany schemat, który jest jednocześnie dokumentacją API</li>
<li>klient określa kształt odpowiedzi, więc nie pobiera zbędnych danych</li>
<li>zmiany bez numerów wersji: nowe pola się dodaje, a stare oznacza dyrektywą <code>@deprecated</code></li>
</ul>

</div>
<div>

**Koszty**
<ul class="cons">
<li>trudniej korzystać z cache HTTP, bo zapytania zwykle wysyła się metodą POST na jeden adres</li>
<li>dowolność zapytań wymaga limitów głębokości i kosztu, inaczej łatwo przeciążyć serwer</li>
<li>uprawnienia trzeba sprawdzać na poziomie pojedynczych pól</li>
<li>błędy często są zwracane w odpowiedzi 200, w tablicy <code>errors</code></li>
<li>serwer jest bardziej złożony</li>
</ul>

</div>
</div>

<div class="takeaway">

GraphQL najlepiej sprawdza się jako API dla interfejsów użytkownika (Backend for Frontend, BFF), gdy wielu klientów potrzebuje różnych widoków tych samych danych.

</div>

---

# gRPC: zdalne wywołania w nowoczesnym wydaniu

gRPC wywodzi się z wewnętrznego systemu RPC firmy Google. W 2015 roku został udostępniony jako open source, a dziś rozwija się w ramach CNCF. Wraca do stylu operacji znanego z RPC i SOAP, ale stawia na wydajność, streaming i obsługę wielu języków.

<div class="cols">
<div>

- Działa w podejściu **contract-first**: interfejs usługi definiuje się w pliku `.proto`.
- Na podstawie kontraktu generuje się kod klienta i serwera w wielu językach programowania.
- Dane są przesyłane w binarnym formacie **Protocol Buffers**.
- Jako transport wykorzystuje **HTTP/2**.

</div>
<div>

**Co daje HTTP/2**
- **multipleksowanie**: wiele równoległych strumieni w jednym połączeniu TCP, bez czekania na zakończenie wcześniejszych żądań,
- **kompresję nagłówków**, która zmniejsza narzut każdego wywołania,
- binarne ramki i kontrolę przepływu,
- długotrwałe strumienie w obu kierunkach, które umożliwiają streaming dużych lub wieloczęściowych danych.

</div>
</div>

---

# Plik `.proto` jako kontrakt

Plik `.proto` definiuje strukturę komunikatów i metody usług. Jest jedynym źródłem prawdy (single source of truth) o API, niezależnym od implementacji i języka programowania.

<div class="cols">
<div>

```proto
syntax = "proto3";
package payments.v1;

service Payments {
  rpc Pay (PayRequest) returns (PayReply);
  rpc WatchTransactions (WatchRequest)
      returns (stream TransactionEvent);
}

message PayRequest {
  string reference     = 1;
  string card_number   = 2;
  string amount        = 3;  // kwota dziesiętna jako tekst
  string currency_code = 4;
}
```

</div>
<div>

- **Usługa** (`service`) grupuje metody. Każda metoda ma określony typ żądania i typ odpowiedzi.
- **Komunikaty** (`message`) określają strukturę danych przesyłanych między klientem a serwerem.
- Pola mogą być typami skalarnymi, zagnieżdżonymi komunikatami, listami (`repeated`), mapami (`map`), enumami lub wariantami (`oneof`).
- Generatory tworzą na podstawie kontraktu silnie typowane klasy klienta i serwera, co ogranicza ryzyko niezgodności.
- Wersję API często umieszcza się w nazwie pakietu, np. `payments.v1`.

<p class="small">Protobuf nie ma wbudowanego typu dziesiętnego. Kwoty przesyła się jako tekst albo jako parę: część całkowita i część ułamkowa.</p>

</div>
</div>

---

# Protocol Buffers i zmiany kontraktu

Protobuf to binarny format serializacji, mniejszy i szybszy od JSON. Z punktu widzenia architektury najważniejsze jest to, że pozwala bezpiecznie zmieniać kontrakt.

<div class="cols">
<div>

- W komunikacie przesyłanym przez sieć nie ma nazw pól, są tylko ich **numery**. To numer pola, a nie jego nazwa, stanowi właściwy kontrakt.
- Odbiorca **pomija nieznane pola**, a brakującym przypisuje wartości domyślne. Dzięki temu starszy klient współpracuje z nowszym serwerem i odwrotnie.
- Precyzyjne typy zwiększają niezawodność komunikacji.
- Sztywny kontrakt utrudnia jednak pracę z danymi o zmiennej strukturze.

</div>
<div>

**Zasady bezpiecznych zmian**

| Zmiana | Czy jest zgodna wstecz? |
|---|---|
| dodanie pola z nowym numerem | tak |
| zmiana nazwy pola | tak w przesyłanych danych, ale wymaga zmian w kodzie |
| usunięcie pola i zarezerwowanie jego numeru (`reserved`) | tak |
| ponowne użycie numeru usuniętego pola | **nie**: dane zostaną błędnie odczytane |
| zmiana typu pola | zwykle **nie** |

</div>
</div>

---

# Tryby wywołań w gRPC

Dzięki HTTP/2 gRPC obsługuje nie tylko klasyczny schemat żądanie–odpowiedź, ale też strumienie danych w jedną lub w obie strony. To istotna przewaga nad REST i SOAP.

| Tryb | Przebieg | Przykład |
|---|---|---|
| **unary** | klient wysyła jedno żądanie i otrzymuje jedną odpowiedź | obciążenie karty |
| **server streaming** | klient wysyła jedno żądanie, a serwer odsyła wiele komunikatów | podgląd transakcji karty na żywo |
| **client streaming** | klient wysyła wiele komunikatów, a serwer odpowiada raz | import paczki transakcji z pliku |
| **bidirectional streaming** | obie strony wysyłają komunikaty jednocześnie i niezależnie | bieżąca ocena ryzyka napływających transakcji |

- **Stuby** klienta i serwera są generowane z plików `.proto`. Nie trzeba ręcznie pisać kodu komunikacji, a wywołania są silnie typowane, w wersji blokującej i asynchronicznej.
- Każde wywołanie może mieć **deadline**, który można przekazywać dalej w łańcuchu usług. Dzięki temu kolejne usługi wiedzą, ile czasu zostało na odpowiedź.
- **Metadane**, odpowiednik nagłówków HTTP, przenoszą dane uwierzytelniające, identyfikatory korelacji i informacje do tracingu.

---

# Obsługa błędów w gRPC

gRPC używa ustandaryzowanych kodów statusu, wspólnych dla wszystkich języków programowania. Dzięki temu klienci napisani w różnych technologiach mogą reagować na błędy w ten sam sposób.

<div class="cols">
<div>

- Każda odpowiedź ma **kod statusu** i opcjonalny komunikat.
- Rozszerzony model błędów (`google.rpc.Status`) pozwala dołączyć **szczegóły**: które pole jest niepoprawne, który warunek nie został spełniony, po jakim czasie ponowić próbę.
- Kod statusu podpowiada klientowi, co zrobić: poprawić żądanie, poinformować użytkownika albo ponowić wywołanie.
- Wyjątki domenowe zamienia na kody statusu adapter, tak samo jak w przypadku HTTP.

</div>
<div>

| Status | Znaczenie | Odpowiednik HTTP |
|---|---|---|
| `INVALID_ARGUMENT` | niepoprawne dane wejściowe | 400 |
| `NOT_FOUND` | zasób nie istnieje | 404 |
| `FAILED_PRECONDITION` | stan systemu nie pozwala wykonać operacji | 400 (w REST często 422) |
| `ABORTED` | konflikt równoległych zmian | 409 |
| `UNAVAILABLE` | usługa jest chwilowo niedostępna, można ponowić | 503 |
| `DEADLINE_EXCEEDED` | przekroczono deadline | 504 |

</div>
</div>

---

# gRPC: kiedy warto, a kiedy nie

gRPC jest wydajny i precyzyjny, ale wymaga narzędzi i infrastruktury, które nie zawsze są dostępne.

<div class="cols">
<div class="card good">

## Zalety

<ul class="pros">
<li>wysoka wydajność i małe opóźnienia dzięki binarnym komunikatom</li>
<li>wbudowany streaming w obu kierunkach</li>
<li>silnie typowane kontrakty generowane z plików <code>.proto</code>, więc niezgodności wykrywa kompilator</li>
<li>generowane biblioteki klienckie dla wielu języków</li>
<li>deadline'y i metadane wbudowane w protokół</li>
</ul>

</div>
<div class="card bad">

## Ograniczenia

<ul class="cons">
<li>binarnych komunikatów nie da się czytać bez narzędzi, co utrudnia debugowanie</li>
<li>ograniczona obsługa w przeglądarkach: gRPC-Web wymaga dodatkowego proxy</li>
<li>trzeba poznać składnię Protocol Buffers i proces generowania kodu</li>
<li>niektóre firewalle i serwery proxy źle obsługują ruch HTTP/2</li>
<li>sztywny kontrakt utrudnia pracę z danymi o zmiennej strukturze</li>
</ul>

</div>
</div>

<div class="takeaway">

gRPC najlepiej sprawdza się w komunikacji wewnętrznej między usługami, gdy liczy się wydajność, a obie strony są rozwijane w tej samej organizacji.

</div>

---

# Messaging: queue i topic

W komunikacji asynchronicznej nadawca i odbiorca nie komunikują się bezpośrednio. Pośredniczy między nimi broker (Message-Oriented Middleware), który przyjmuje komunikaty i dostarcza je odbiorcom.

<div class="cols">
<div>

- Nadawca i odbiorca nie muszą działać w tym samym czasie. To tzw. **temporal decoupling**.
- Broker przechowuje komunikaty, gdy odbiorca nie nadąża z ich przetwarzaniem albo jest chwilowo niedostępny.
- **JMS** (Java Message Service) to standardowe API Javy do wysyłania i odbierania komunikatów. **MDB** (Message-Driven Bean) to komponent EJB uruchamiany po nadejściu komunikatu.
- Komunikat JMS składa się z **nagłówków** (identyfikator, korelacja, adres odpowiedzi, czas ważności), **właściwości** i **treści** (tekst, bajty, mapa, obiekt).

</div>
<div>

<div class="card">

## Queue (point-to-point)

Każdy komunikat trafia do **jednego** odbiorcy. Odbiorcy rywalizują o komunikaty, co pozwala rozłożyć pracę na wiele instancji.

Typowe zastosowanie: **polecenia**, np. „obciąż kartę”.

</div>

<div class="card good mt">

## Topic (publish–subscribe)

Każdy subskrybent otrzymuje **własną kopię** komunikatu.

Typowe zastosowanie: **zdarzenia**, np. „płatność przetworzona”, na które reaguje kilka niezależnych modułów.

</div>

</div>
</div>

---

# Kafka: log zdarzeń

Kafka działa inaczej niż klasyczny broker z kolejkami. Topic jest trwałym, uporządkowanym logiem, z którego komunikaty nie znikają po odczytaniu.

<div class="cols">
<div>

- Topic dzieli się na **partycje**. Każda partycja to uporządkowany ciąg komunikatów.
- **Klucz** komunikatu wyznacza partycję. Kolejność jest zachowana tylko **w obrębie klucza**. Jeśli kluczem jest identyfikator zamówienia, zdarzenia dotyczące jednego zamówienia zostaną odczytane w kolejności ich zapisu.
- **Consumer groups**: w ramach jednej grupy partycje są rozdzielane między instancje (jak w queue), a każda grupa otrzymuje wszystkie komunikaty (jak w topicu).
- Konsument sam zarządza swoją pozycją w logu (offsetem), więc może ponownie przetworzyć historię od dowolnego miejsca.

</div>
<div>

| | Klasyczna kolejka | Log (Kafka) |
|---|---|---|
| po odczytaniu | komunikat jest usuwany | pozostaje do końca okresu retencji |
| kolejność | zwykle w obrębie kolejki | w obrębie partycji, czyli klucza |
| ponowne przetworzenie | niemożliwe | możliwe |
| skalowanie odbiorców | competing consumers | partycje i consumer groups |
| typowa gwarancja | at-least-once | at-least-once |

</div>
</div>

---

# Asynchroniczność na granicy z klientem

Przy przetwarzaniu asynchronicznym klient nie otrzymuje wyniku od razu, tylko potwierdzenie przyjęcia żądania.

<div class="grid4">
<div class="box soft">klient składa zamówienie</div><div class="arr">→</div>
<div class="box core">zamówienie zapisane<span class="sub">wysłano polecenie „płatność zlecona”</span></div><div class="arr">→</div>
<div class="box warm">klient dostaje 202 Accepted<span class="sub">i adres zasobu ze statusem</span></div><div class="arr">→</div>
<div class="box dash">broker dostarcza polecenie</div>
<div></div><div></div><div></div><div></div><div></div><div></div><div class="arr center">↓</div>
<div class="box good">status zamówienia: opłacone lub odrzucone</div><div class="arr">←</div>
<div class="box">zamówienia odbierają zdarzenie</div><div class="arr">←</div>
<div class="box">zdarzenie „płatność przetworzona”</div><div class="arr">←</div>
<div class="box">usługa płatności obciąża kartę</div>
</div>

<div class="cols">
<div>

**Jak klient poznaje wynik?**
- **polling** zasobu ze statusem,
- **webhook**: usługa wywołuje adres podany przez klienta,
- **SSE** lub **WebSocket**: serwer wysyła zmiany do przeglądarki,
- **subscription** w GraphQL, **server streaming** w gRPC.

</div>
<div>

**Polecenie czy zdarzenie?**
- **polecenie** („płatność zlecona”) ma określonego adresata, od którego oczekujemy reakcji,
- **zdarzenie** („płatność przetworzona”) informuje o fakcie, a nadawca nie wie, kto na nie zareaguje,
- **korelacja**: odbiorca odsyła identyfikator z polecenia, więc płatności nie muszą wiedzieć, czym jest zamówienie.

</div>
</div>

---

# Porównanie protokołów

Tabela zestawia omówione style i protokoły. Żaden z nich nie jest najlepszy pod każdym względem, bo każdy optymalizuje inne cechy.

| | SOAP | REST | GraphQL | gRPC | Messaging |
|---|---|---|---|---|---|
| styl | operacje | zasoby | zapytania | operacje | polecenia i zdarzenia |
| kontrakt | WSDL i XSD | OpenAPI (opcjonalny) | schemat (obowiązkowy) | `.proto` (obowiązkowy) | AsyncAPI, Schema Registry |
| format | XML | zwykle JSON | JSON | Protobuf (binarny) | dowolny: JSON, Avro, Protobuf |
| transport | HTTP, JMS, SMTP | HTTP | zwykle HTTP | HTTP/2 | broker |
| interakcja | synchroniczna | synchroniczna | synchroniczna i subskrypcje | synchroniczna i streaming | asynchroniczna |
| cache HTTP | nie | **tak** | utrudniony | nie | nie dotyczy |
| obsługa w przeglądarce | słaba | natywna | natywna | przez proxy | pośrednio |
| typowe zastosowanie | integracje korporacyjne, starsze systemy | publiczne API | API dla frontendów | komunikacja między usługami | procesy rozproszone |

---

# Ten sam błąd domenowy w każdym protokole

Wyjątek „brak wystarczających środków” powstaje w modelu domeny karty. Domena nie wie, jak klient zobaczy ten błąd. Decyduje o tym adapter danego protokołu.

| Protokół | Jak przekazać błąd |
|---|---|
| REST | odpowiedź `422 Unprocessable Content` z treścią w formacie `application/problem+json` |
| SOAP | element `soap:Fault` z kodem wskazującym błąd nadawcy i szczegółami w `Detail` |
| GraphQL | typ `InsufficientFunds` w unii wyników mutacji albo wpis w tablicy `errors` z kodem w polu `extensions` |
| gRPC | status `FAILED_PRECONDITION` ze szczegółami w `google.rpc.Status` |
| messaging | zdarzenie „płatność przetworzona” ze statusem „odrzucona” i przyczyną odrzucenia |

<div class="takeaway">

W każdym protokole warto rozróżniać trzy rodzaje błędów: **błąd klienta** (trzeba poprawić żądanie), **naruszenie reguły biznesowej** (trzeba poinformować użytkownika) i **błąd infrastruktury** (można spróbować ponownie później). Od tego zależy reakcja klienta.

</div>

---

# Jak wybrać protokół

Protokół dobiera się do konkretnej granicy systemu. Trzeba wziąć pod uwagę, kto jest klientem, jakie ma możliwości techniczne i czego potrzebuje.

| Granica | Typowy wybór | Uzasadnienie |
|---|---|---|
| publiczne API dla partnerów | REST z opisem w OpenAPI | powszechność, cache, narzędzia, niski próg wejścia |
| frontend z wieloma różnymi widokami | GraphQL albo REST w modelu BFF | odpowiedź dopasowana do ekranu, mniej wywołań |
| komunikacja wewnętrzna przy dużym ruchu | gRPC | wydajność, typowanie, streaming, deadline'y |
| integracja z istniejącym systemem udostępniającym WSDL | SOAP | gotowy kontrakt, wymagane standardy WS-* |
| proces obejmujący kilka usług | messaging | odporność na niedostępność usług, luźne powiązania |
| powiadomienia w czasie rzeczywistym | subskrypcje GraphQL, SSE, server streaming w gRPC | brak pollingu |

<div class="takeaway">

Wybór protokołów to decyzja makroarchitektoniczna. Niewielki, ustandaryzowany zestaw sposobów integracji ułatwia monitorowanie, zabezpieczenie i utrzymanie systemu.

</div>

---

# Wersjonowanie API

Wersjonowanie to zobowiązanie wobec użytkowników API. Strategię wybiera się na początku projektu, a cykl życia wersji (okres wsparcia, wycofanie, migrację) jasno się komunikuje. Zachowanie kolejnych wersji sprawdzają testy automatyczne.

<div class="cols">
<div>

**Zmiany zgodne wstecz**: dodanie opcjonalnego pola, nowej operacji lub nowego zasobu.
**Zmiany niezgodne**: usunięcie pola lub zmiana jego znaczenia, nowe pole obowiązkowe, zmiana typu. Tylko takie zmiany wymagają nowej wersji.

| Protokół | Sposób wersjonowania |
|---|---|
| gRPC | numery pól, nowy pakiet `v2` |
| GraphQL | rozwijanie schematu i `@deprecated` |
| messaging | wersja w nazwie typu zdarzenia, Schema Registry |

</div>
<div>

**REST: trzy strategie**

| Gdzie | Przykład | Cechy |
|---|---|---|
| ścieżka URI | `/v1/orders` | wersja jest widoczna, łatwo cache'ować i kierować ruch |
| nagłówek | `X-API-Version: 2` | adresy pozostają bez zmian |
| media type | `application/vnd.shop.v3+json` | wykorzystuje content negotiation |

**Wycofywanie wersji**: nagłówki `Deprecation` (RFC 9745) i `Sunset` z datą wycofania (RFC 8594), przewodnik migracji, okres przejściowy z monitorowaniem użycia, informacja w kilku kanałach.

</div>
</div>

---

# Kontrakty i testy kontraktowe

Kontrakt sieciowy ma dwie strony, które są rozwijane i wdrażane niezależnie. Testy każdej strony z osobna nie wykryją, że strony przestały do siebie pasować.

<div class="cols">
<div>

**Test kontraktowy** sprawdza obie strony względem wspólnego opisu:
- **provider** (dostawca): czy na dane żądanie odpowiada zgodnie z kontraktem,
- **consumer** (konsument): czy potrafi przetworzyć odpowiedź opisaną w kontrakcie.

W podejściu **consumer-driven contracts** (np. Pact, Spring Cloud Contract) to konsumenci określają, czego potrzebują, a dostawca sprawdza, czy spełnia oczekiwania wszystkich konsumentów.

Prostsza wersja: przykładowe komunikaty przechowywane razem z kontraktem i sprawdzane w testach obu stron.

</div>
<div>

**Jak projektować kontrakt sieciowy**
- **prosto i płasko**, bez hierarchii klas specyficznych dla języka programowania,
- w **Published Language**, a nie w języku wewnętrznego modelu domeny,
- zgodnie z zasadą **Tolerant Reader**: konsument ignoruje nieznane pola i nie zakłada ich kolejności,
- z jasno określonym **właścicielem**, który decyduje o zmianach,
- z zachowaniem **zgodności wstecz** tak długo, jak to możliwe.

</div>
</div>

---

# Niezawodność wywołań zdalnych

Sieć bywa zawodna, więc każde wywołanie zdalne trzeba projektować z myślą o awariach. Istnieje zestaw sprawdzonych mechanizmów, które ograniczają ich skutki.

<div class="cols">
<div>

| Mechanizm | Przed czym chroni |
|---|---|
| **timeout** / deadline | blokowaniem wątków i połączeń |
| **retry** z exponential backoff | chwilowymi awariami |
| **klucz idempotentności** | wykonaniem operacji dwa razy przy ponownej próbie |
| **bulkhead** (limit współbieżności) | wyczerpaniem zasobów przez jedną zależność |
| **circuit breaker** | zasypywaniem żądaniami usługi, która nie działa |
| **fallback** | całkowitym brakiem odpowiedzi |

</div>
<div>

- Retry stosuje się tylko do operacji **idempotentnych** albo zabezpieczonych kluczem idempotentności.
- Ponowienia na kilku poziomach się mnożą: trzy próby na trzech poziomach to 27 wywołań.
- **Antywzorzec**: synchroniczne wywołanie innej usługi wewnątrz transakcji bazodanowej. Połączenie z bazą pozostaje zajęte przez cały czas oczekiwania na odpowiedź.
- Komunikacja asynchroniczna nie usuwa tych problemów, tylko je przenosi: do brokera i do odbiorcy, który musi być idempotentny.

</div>
</div>

<div class="takeaway">

Jak w takim razie zachować spójność danych, skoro operacji obejmującej kilka usług nie da się zamknąć w jednej transakcji?

</div>

---

# Granice transakcji

Gdy logika jest podzielona na agregaty, moduły i usługi, jedna transakcja bazodanowa nie obejmuje już całej operacji biznesowej. Trzeba świadomie zdecydować, gdzie kończy się silna spójność.

<div class="cols">
<div>

- **Transakcja ACID** obejmuje jeden agregat w jednym module i jednej bazie danych.
- Między agregatami, modułami i usługami obowiązuje **eventual consistency**: system osiąga spójny stan po pewnym czasie, a nie natychmiast.
- To nie jest wyłącznie kompromis techniczny. Wiele procesów biznesowych działa w ten sposób od zawsze: przelew między bankami, wysyłka po zaksięgowaniu płatności, zwrot po rozpatrzeniu reklamacji.
- Projektując proces, trzeba ustalić, jakie stany pośrednie są dopuszczalne i jak pokazać je użytkownikowi.

</div>
<div class="card bad">

## Dlaczego nie transakcja rozproszona (2PC)

- Koordynator blokuje zasoby wszystkich uczestników aż do decyzji o zatwierdzeniu.
- System jest dostępny tylko wtedy, gdy dostępni są wszyscy uczestnicy transakcji.
- Awaria koordynatora pozostawia uczestników w stanie niepewności.
- Wiele baz NoSQL i nowoczesnych brokerów komunikatów nie obsługuje transakcji rozproszonych (XA).
- Usługi, które miały być niezależne, zostają silnie powiązane w czasie.

</div>
</div>

---

# Dual write i Transactional Outbox

Typowa operacja zmienia dane w bazie i publikuje zdarzenie w brokerze. To dwie operacje na dwóch różnych systemach, więc jedna z nich może się nie powieść.

<div class="cols">
<div>

**Problem dual write**
- zapis w bazie się udał, a publikacja nie: zdarzenie przepada, a zamówienie bez końca czeka na płatność,
- publikacja się udała, a zapis nie: inne usługi reagują na coś, co się nie wydarzyło.

**Transactional Outbox**: zdarzenie zapisuje się w osobnej tabeli w **tej samej transakcji** co zmianę biznesową. Osobny proces publikuje je po zatwierdzeniu transakcji i ponawia próby do skutku.

**Inbox**: odbiorca zapisuje identyfikator przetworzonego komunikatu w tej samej transakcji co skutki jego obsługi. Dzięki temu rozpoznaje duplikaty.

</div>
<div>

<div class="vchain">
<div class="box core">transakcja: zmiana agregatu i wpis w tabeli outbox</div>
<div class="arr">↓ po commicie</div>
<div class="box soft">relay publikuje zdarzenie i oznacza wpis jako wysłany</div>
<div class="arr">↓</div>
<div class="box warm">broker komunikatów</div>
<div class="arr">↓</div>
<div class="box soft">odbiorca: wpis w tabeli inbox i obsługa zdarzenia w jednej transakcji</div>
</div>

</div>
</div>

---

# Gwarancje dostarczenia i idempotentność

Brokery komunikatów oferują różne gwarancje dostarczenia. W praktyce najczęściej stosuje się gwarancję at-least-once, więc odbiorca musi być przygotowany na duplikaty.

| Gwarancja | Znaczenie | Konsekwencje |
|---|---|---|
| **at-most-once** | komunikat może zginąć, ale nie zostanie dostarczony dwa razy | dopuszczalne np. dla metryk, ale nie dla płatności |
| **at-least-once** | komunikat nie zginie, ale może zostać dostarczony kilka razy | najczęstsza gwarancja; odbiorcy muszą być idempotentni |
| **exactly-once** | komunikat nie zginie i nie zostanie dostarczony dwa razy | w praktyce osiąga się ją jako at-least-once w połączeniu z idempotentnym odbiorcą |

<div class="cols">
<div>

**Jak zapewnić idempotentność odbiorcy**
- pomijać duplikaty na podstawie identyfikatora komunikatu (inbox),
- stosować operacje z natury idempotentne: „ustaw status” zamiast „dodaj kwotę”, upsert zamiast insert,
- przed zmianą sprawdzać wersję lub oczekiwany stan danych.

</div>
<div>

**Kolejność komunikatów**
- jest zachowana najwyżej w obrębie jednego klucza lub jednej partycji,
- odbiorca musi albo poradzić sobie ze zdarzeniami w innej kolejności, albo je odrzucić i poczekać na ponowne dostarczenie.

</div>
</div>

---

# CQS i CQRS

Oddzielenie zapisu od odczytu można stosować zarówno na poziomie pojedynczej metody, jak i całego systemu.

<div class="cols">
<div>

**Command Query Separation (CQS)**, zasada sformułowana przez Bertranda Meyera, mówi, że każda metoda powinna być albo:
- **komendą** (command), która zmienia stan i nic nie zwraca,
- albo **zapytaniem** (query), które zwraca wynik i nie zmienia stanu.

Inaczej mówiąc, zadanie pytania nie powinno zmieniać odpowiedzi. Zapytania można bezpiecznie powtarzać, cache'ować i wykonywać w dowolnej kolejności.

</div>
<div>

**Command Query Responsibility Segregation (CQRS)**, spopularyzowane przez Grega Younga i Udiego Dahana, przenosi tę zasadę na poziom klas, komponentów i całego systemu. Zapis i odczyt mają **osobne modele**.

Dlaczego warto:
- model zapisu chroni niezmienniki (agregaty), a odczyt potrzebuje wielu widoków łączących dane z różnych miejsc,
- odczytów jest zwykle znacznie więcej niż zapisów i mają one inne wymagania wydajnościowe,
- jeden model, który ma obsłużyć oba cele, zwykle nie sprawdza się dobrze w żadnym z nich.

</div>
</div>

---

# Elementy CQRS

W pełnej architekturze CQRS są dwie ścieżki: komendy, które zmieniają stan, i zapytania, które go odczytują. Łączą je zdarzenia.

<div class="grid5">
<div class="box soft">Command</div><div class="arr">→</div>
<div class="box">Command Bus</div><div class="arr">→</div>
<div class="box">Command Handler</div><div class="arr">→</div>
<div class="box core">agregat</div><div class="arr">→</div>
<div class="box">write database</div>
<div></div><div></div><div></div><div></div><div></div><div></div><div></div><div></div><div class="arr center">↓</div>
<div class="box soft">Query</div><div class="arr">→</div>
<div class="box good">read database</div><div class="arr">←</div>
<div class="box">Event Handler (projekcja)</div><div class="arr">←</div>
<div class="box">Event Bus</div><div class="arr">←</div>
<div class="box warm">Event</div>
</div>

| Element | Rola |
|---|---|
| Command | obiekt opisujący intencję użytkownika, np. „dodaj transakcję” |
| Command Bus | kolejka lub router, który przekazuje komendę do właściwego handlera |
| Command Handler | sprawdza komendę, zmienia agregat, zapisuje go i przekazuje dalej powstałe zdarzenia |
| Event | obiekt opisujący zmianę, która zaszła w domenie |
| Event Bus, Event Handler | przekazują zdarzenia do handlerów, które aktualizują read database |
| Read Database Abstraction | kontrakt dostępu do widoków, niezależny od modelu zapisu |

---

# Trzy stopnie CQRS

CQRS nie oznacza od razu osobnych baz danych. Można go wprowadzać stopniowo, a każdy kolejny stopień daje więcej możliwości za wyższą cenę.

| Stopień | Zapis | Odczyt | Spójność | Koszt |
|---|---|---|---|---|
| **CQRS light** | agregat i repozytorium tylko do zapisu | osobne zapytania SQL do tej samej bazy, bez ładowania agregatu | silna | niski |
| **osobny read store** | agregat i outbox | dokumenty w osobnej bazie, budowane na podstawie zdarzeń | ostateczna | średni |
| **Event Sourcing** | strumień zdarzeń | wyłącznie projekcje | ostateczna | wysoki |

Projekcje, które budują read model, muszą:
- być **idempotentne**, czyli dawać ten sam wynik po ponownym przetworzeniu tego samego zdarzenia,
- **radzić sobie z kolejnością** zdarzeń, np. przez upsert i sprawdzanie wersji,
- dawać się **odtworzyć**, czyli zbudować read model od nowa w dowolnym momencie.

<div class="takeaway">

Eventual consistency jest widoczna dla użytkownika: odczyt zaraz po zapisie może zwrócić poprzedni stan. Trzeba to uwzględnić w interfejsie i w API, np. zwracając wynik komendy zamiast od razu odczytywać go z read modelu.

</div>

---

# Event Sourcing

W CQRS zdarzenia służą do synchronizacji bazy zapisu z bazą odczytu. Event Sourcing idzie dalej: zdarzenia stają się jedynym źródłem prawdy, a bieżący stan jest z nich wyliczany.

<div class="cols">
<div>

- Stan agregatu odtwarza się, stosując po kolei zdarzenia zapisane w **Event Store**.
- Event Store działa w trybie **append-only**: zdarzeń się nie zmienia ani nie usuwa.
- Numer wersji strumienia zapewnia **optimistic locking**: z dwóch równoległych zapisów do tego samego strumienia powiedzie się tylko jeden.
- **Snapshoty** zapisywane co określoną liczbę zdarzeń przyspieszają odtwarzanie stanu.
- Jeśli port repozytorium jest dobrze zaprojektowany, use case'y nie zauważą zmiany sposobu przechowywania danych.
- Testy przyjmują naturalną postać: **given** (wcześniejsze zdarzenia), **when** (komenda), **then** (nowe zdarzenia).

</div>
<div>

**Zalety**
<ul class="pros">
<li>pełny audyt i historia wszystkich zmian</li>
<li>możliwość odtworzenia stanu z dowolnego momentu</li>
<li>nowe widoki można zbudować na podstawie całej historii</li>
<li>gotowe zdarzenia do integracji z innymi usługami</li>
</ul>

**Koszty**
<ul class="cons">
<li>wersjonowanie zdarzeń i ich konwersja przy zmianie formatu (upcasting)</li>
<li>historii nie można poprawić; błędy koryguje się kolejnymi zdarzeniami</li>
<li>read modele są niezbędne</li>
<li>niezmienny log utrudnia realizację prawa do usunięcia danych (RODO)</li>
</ul>

</div>
</div>

---

# Saga

Saga to proces biznesowy obejmujący wiele modułów lub usług, z których każda ma własną transakcję. Pojęcie wprowadzili Hector Garcia-Molina i Kenneth Salem w 1987 roku.

<div class="cols-wide">
<div>

- Zamiast jednej transakcji rozproszonej saga jest **sekwencją transakcji lokalnych**.
- Każdy krok ma **kompensację**, czyli operację, która odwraca jego skutki biznesowe.
- Kompensacja ma charakter **biznesowy** (zwrot środków na kartę), a nie techniczny (rollback transakcji, która została już zatwierdzona).
- Ukończone kroki kompensuje się w **odwrotnej kolejności**.

**Wymagania praktyczne**
- idempotentne kroki, bo komunikaty mogą dotrzeć więcej niż raz,
- trwały zapis stanu procesu, żeby po restarcie wiedzieć, gdzie proces się zatrzymał,
- timeouty i ponawianie kroków,
- plan na wypadek nieudanej kompensacji.

</div>
<div>

<div class="vchain">
<div class="box core">obciąż kartę</div>
<div class="arr">↓ powodzenie</div>
<div class="box core">zleć wysyłkę</div>
<div class="arr">↓ odmowa</div>
<div class="box bad">zwróć środki (kompensacja)</div>
<div class="arr">↓</div>
<div class="box soft">anuluj zamówienie</div>
</div>

</div>
</div>

---

# Orkiestracja i choreografia

Sagą może sterować centralny orkiestrator albo może ona przebiegać bez koordynatora, gdy uczestnicy reagują na swoje zdarzenia.

<div class="cols">
<div>

**Orkiestracja**
<div class="chain">
<div class="box soft">płatności</div><div class="arr">⇄</div>
<div class="box core">orkiestrator (zamówienia)</div><div class="arr">⇄</div>
<div class="box soft">wysyłki</div>
</div>
<p class="diagram-caption">orkiestrator wysyła polecenia i czeka na odpowiedzi</p>

**Choreografia**
<div class="chain">
<div class="box soft">zamówienia</div><div class="arr">→</div>
<div class="box soft">płatności</div><div class="arr">→</div>
<div class="box soft">wysyłki</div>
</div>
<p class="diagram-caption">każdy uczestnik reaguje na zdarzenia i publikuje własne</p>

</div>
<div>

| | Orkiestracja | Choreografia |
|---|---|---|
| przebieg | w jednym miejscu | rozproszony |
| stan procesu | jawnie zapisany | wynika ze statusów |
| komunikaty | polecenia i odpowiedzi | zdarzenia |
| zależności | orkiestrator zna uczestników | uczestnicy znają zdarzenia |
| timeouty | wbudowane | trzeba je dodać |
| ryzyko | rozrośnięty orkiestrator | trudny do prześledzenia proces |

</div>
</div>

<div class="takeaway">

Orkiestracja opiera się na poleceniach (queue), a choreografia na zdarzeniach (topic). Wybór stylu sagi jest więc też wyborem stylu komunikacji.

</div>

---

# Gdy reguły zmieniają się szybciej niż wydania

Nawet dobrze zaprojektowany model domeny wymaga wydania nowej wersji aplikacji przy każdej zmianie reguły. W niektórych dziedzinach to zbyt wolno, a reguły powinni móc zmieniać sami eksperci.

<div class="cols">
<div>

**Typowe pytania**
- Jak sprawić, żeby oprogramowanie nadążało za zmianami w biznesie?
- Jak dokładnie określić, **od kiedy** obowiązuje zmiana?
- Jak wprowadzić zmianę tylko **dla wybranych klientów**?
- Jak ustalić, **kto** może zmieniać poszczególne reguły, i jak zachować **historię** zmian?

</div>
<div>

**Silniki reguł i procesów biznesowych**
- wykonują i monitorują reguły zapisane w **formalnym języku**,
- pozwalają wydzielić reguły i procesy z kodu aplikacji,
- umożliwiają ich tworzenie i zmienianie **poza kodem**, także przez analityków biznesowych.

W architekturze z logiką w centrum silnik reguł jest szczegółem technicznym. Znajduje się za portem (np. „polityka prowizji”), a model domeny decyduje, kiedy z niego skorzystać.

</div>
</div>

---

# Silniki reguł i procesów w praktyce

Logikę wydzieloną poza kod najczęściej zapisuje się jako reguły decyzyjne albo jako wykonywalne modele procesów.

<div class="cols">
<div class="card">

## Reguły: Drools, DMN

- Reguły zapisuje się w postaci **„jeśli warunek, to akcja”** albo jako **tablice decyzyjne** (decision tables).
- Silnik dopasowuje fakty do reguł i wykonuje te, które są spełnione.
- Sprawdza się przy wielu niezależnych, często zmienianych regułach: taryfach, rabatach, ocenie ryzyka.

<ul class="cons">
<li>logikę trudniej testować i śledzić</li>
<li>ryzyko zarządzania regułami jak arkuszem kalkulacyjnym, bez kontroli wersji i testów</li>
</ul>

</div>
<div class="card warm">

## Procesy: BPMN 2.0

- Proces ma postać **wykonywalnego diagramu**: zadań (tasks), bramek (gateways), zdarzeń, timerów i kompensacji.
- Silnik procesów (np. Camunda, Flowable) przechowuje stan każdej instancji procesu i pilnuje timeoutów.
- W praktyce jest to **orkiestrowana saga**, którą rozumieją także osoby z biznesu.

<ul class="cons">
<li>dodatkowa infrastruktura i kolejny model do utrzymania</li>
<li>pokusa przeniesienia całej logiki biznesowej do diagramów</li>
</ul>

</div>
</div>

---

# Od klasy do systemu: jedna idea

Wszystkie omówione zagadnienia to zastosowania tej samej zasady na różnych poziomach: logikę biznesową chroni granica z kontraktem, a przestrzeganie tej granicy sprawdza się automatycznie.

| Poziom | Granica | Kontrakt | Jak egzekwujemy granicę |
|---|---|---|---|
| agregat | metody biznesowe | niezmienniki | kompilator, testy domeny |
| aplikacja | porty | interfejsy wyrażone językiem aplikacji | reguła zależności, testy architektury |
| moduł | publiczne API | fasada, zdarzenia | moduły Maven, JPMS, Spring Modulith |
| usługa | protokół | WSDL, OpenAPI, schemat GraphQL, `.proto` | testy kontraktowe |
| proces | komunikaty | schematy zdarzeń, saga | outbox, idempotentność, timeouty |

1. Zaczynamy od **modelu domeny** i reguły zależności. Framework, baza danych i protokół to szczegóły.
2. Protokół wybieramy **dla konkretnej granicy**, a nie dla całego systemu. Każdy protokół to tylko adapter.
3. Rozproszenie daje niezależność kosztem spójności. Często wystarcza **modularny monolit**.
4. Każda decyzja architektoniczna jest kompromisem. Jej kontekst i konsekwencje zapisujemy w **ADR**.

---

# Literatura

<div class="cols">
<div>

**Architektura i modelowanie**
- E. Evans, *Domain-Driven Design: Tackling Complexity in the Heart of Software*
- R. C. Martin, *Clean Architecture*
- T. Hombergs, *Get Your Hands Dirty on Clean Architecture*
- M. Richards, N. Ford, *Fundamentals of Software Architecture*
- N. Ford, M. Richards i in., *Software Architecture: The Hard Parts*
- C. Richardson, *Microservices Patterns*
- E. Gamma i in., *Design Patterns*

</div>
<div>

**Komunikacja i procesy**
- R. Fielding, *Architectural Styles and the Design of Network-based Software Architectures* (2000)
- RFC 9110 (semantyka HTTP), RFC 9457 (Problem Details)
- specyfikacja GraphQL: spec.graphql.org
- gRPC i Protocol Buffers: grpc.io, protobuf.dev
- H. Garcia-Molina, K. Salem, *Sagas* (1987)
- zasady ISA: isa-principles.org
- model C4: c4model.com

</div>
</div>
