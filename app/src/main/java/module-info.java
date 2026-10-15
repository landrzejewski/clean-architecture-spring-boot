/*
 Composition root. Wymaga modułów kontekstów, żeby trafiły do grafu modułów,
 ale nie ma dostępu do ich wnętrza (nie eksportują pakietów).
 */
open module pl.fullstackdeveloper.app {

    requires pl.fullstackdeveloper.payments;
    requires pl.fullstackdeveloper.orders;

    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;

}
