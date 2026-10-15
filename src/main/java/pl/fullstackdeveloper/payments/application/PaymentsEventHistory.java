package pl.fullstackdeveloper.payments.application;

import java.util.function.Consumer;

/*
 Port wyjściowy: odtworzenie całej historii zdarzeń modułu w kolejności zapisu.
 Dzięki Event Sourcingowi dowolny model odczytu można zbudować od zera (replay).
 */
public interface PaymentsEventHistory {

    void replay(Consumer<PaymentsEvent> consumer);

}
