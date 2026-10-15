# 21 – Saga: choreografia

> Moduł 4: Wzorce zaawansowane · poprzedni: `20-saga-orchestration` · koniec szkolenia

## Cel
Zrealizować **ten sam proces** co w `20` (płatność → wysyłka → potwierdzenie, a przy błędzie wysyłki zwrot środków)
**bez orkiestratora**. Moduły reagują na fakty publikowane przez inne moduły. Porównać oba warianty.

## Przebieg
```
orders    ── OrderPlaced ─────────────┬──────────────► payments: obciąż kartę ── PaymentCompleted / PaymentFailed
                                      └──────────────► shipping: zapamiętaj adres
payments  ── PaymentCompleted ───────────────────────► shipping: zaplanuj ──── ShipmentScheduled / ShipmentFailed
          └─ PaymentCompleted / PaymentFailed ───────► orders: PAID / REJECTED
shipping  ── ShipmentScheduled ──────────────────────► orders: CONFIRMED
          └─ ShipmentFailed ─────────────────────────► payments: zwrot ─────── PaymentRefunded ──► orders: CANCELLED
```

## Co się zmieniło
```bash
git diff 20-saga-orchestration..21-saga-choreography --stat
```
| Usunięte (orkiestracja) | Dodane (choreografia) |
|---|---|
| `OrderSagaOrchestrator`, `OrderSaga`, `SagaStep`, `SagaStatus`, `order_sagas`, `SagaTimeoutScheduler`, `GET …/saga` | moduł `events` ze wspólnymi zdarzeniami procesu |
| polecenia `PaymentRequested`, `RefundRequested`, `ShipmentRequested` | fakty `OrderPlaced`, `PaymentCompleted`, `ShipmentFailed`, … |
| porty `Payments`, `Shipping` w zamówieniach | port `OrderEvents` + `OrderProgressService` (reakcje zamówień) |
| | `PaymentsProcessListener` (+ `OrderPayment`), `ShippingProcessListener` (+ `PendingShipment`) |
| | `EventPublicationRetryScheduler`: automatyczne ponawianie nieudanych publikacji |

`SagaEndToEndTest` jest prawie identyczny jak w `20`, bo zewnętrzne zachowanie systemu się nie zmieniło.

## Orkiestracja vs choreografia
| | Orkiestracja (`20`) | Choreografia (`21`) |
|---|---|---|
| Gdzie jest proces | w jednej klasie (`OrderSagaOrchestrator`) | rozproszony po listenerach trzech modułów |
| Stan procesu | jawny, trwały (`order_sagas`), endpoint `/saga` | niejawny: status zamówienia + lokalne tabele uczestników |
| Komunikaty | polecenia (zrób X) + odpowiedzi | fakty (stało się X) |
| Zależności | orkiestrator zna uczestników | wszyscy znają wspólny język zdarzeń (`events`) |
| Timeouty, zawieszone procesy | w orkiestratorze | trzeba dobudować (monitoring, „process tracker”) |
| Dodanie kroku | zmiana w orkiestratorze | nowy listener, ale trudno ocenić wpływ na całość |
| Dobre dla | długich, złożonych procesów z kompensacjami | krótkich procesów, luźno powiązanych zespołów |

## Na co zwrócić uwagę
- **Moduł `events`**: bez niego `orders → payments → shipping → orders` tworzą cykl, a `ApplicationModularityTest` by to wykrył. Choreografia wymaga wspólnego kontraktu zdarzeń.
- **Kolejność**: `PaymentCompleted` może dotrzeć do wysyłek przed `OrderPlaced`. Listener rzuca wyjątek, a publikacja jest ponawiana (`EventPublicationRetryScheduler`).
- **Lokalne kopie danych**: płatności pamiętają kwotę i kartę (`OrderPayment`), bo nikt im nie powie, ile zwrócić.
- **Nieudana kompensacja** jest tylko logowana. Nie ma miejsca, które „wie”, że proces utknął. To główny koszt choreografii.

## Kiedy NIE stosować
Gdy proces ma wiele kroków i kompensacji, wymaga timeoutów i wglądu w stan („gdzie jest moje zamówienie?”).
Wtedy wybierz orkiestrację.

## Pytania do dyskusji
- Jak odpowiedzieć na pytanie „ile zamówień utknęło w płatności od godziny?” w każdym z wariantów?
- Czy da się mieszać oba style (np. choreografia między kontekstami, orkiestracja wewnątrz)?
- Kto jest właścicielem modułu `events`?

## Uruchomienie
```bash
./mvnw test
./mvnw spring-boot:run
```

---

## Ćwiczenie końcowe: rozproszona saga „provisioning infrastruktury”
Zaimplementuj sagę (wariant do wyboru, oba dla chętnych) dla tworzenia maszyny wirtualnej. Kroki są symulowane: rekord w bazie + opóźnienie.

```
utwórz VM → podłącz wolumen → skonfiguruj sieć/VLAN → zarejestruj w service mesh → dodaj do monitoringu
```
Kroki 1–2 należą do `compute-service`, a 3–5 do `network-service`. Mogą to być moduły Modulith albo, dla chętnych, dwie aplikacje.

Kryteria akceptacji:
1. Bez błędów saga kończy się sukcesem i istnieją wszystkie zasoby.
2. Błąd w kroku 3 kompensuje kroki 2 i 1. Po stronie sieci nie zostaje żaden zasób.
3. Błąd w kroku 5 kompensuje w kolejności: mesh → sieć → wolumen → VM. Kolejność widać w logach lub znacznikach czasu.
4. Ponowne dostarczenie kroku lub kompensacji nie tworzy duplikatu i nie usuwa niczego dwa razy (idempotentność).
5. Po restarcie aplikacji w trakcie sagi proces kończy się stanem terminalnym bez ręcznej akcji (recovery).
6. Krok bez odpowiedzi jest ponawiany, a po limicie prób kompensowany (timeout).

Wstrzykiwanie błędów: parametr `failAtStep` w żądaniu utworzenia zasobu.
