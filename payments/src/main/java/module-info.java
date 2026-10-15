/*
 Moduł płatności NIE eksportuje żadnego pakietu - domena, use case'y i adaptery są niedostępne
 dla innych modułów w czasie kompilacji, mimo że klasy są public.
 Z zewnątrz płatności są osiągalne wyłącznie przez kontrakt z modułu payments.api
 (implementację wstrzykuje Spring) oraz przez REST.

 "open module" = wszystkie pakiety otwarte do głębokiej refleksji (Spring, Hibernate, Jackson),
 ale żaden nie jest eksportowany do kompilacji. exports != opens.
 */
open module pl.fullstackdeveloper.payments {

    requires pl.fullstackdeveloper.common;
    requires pl.fullstackdeveloper.payments.api;

    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.jdbc;
    requires spring.tx;
    requires spring.web;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires org.slf4j;

}
