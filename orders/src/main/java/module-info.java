/*
 Zamówienia wymagają (requires) tylko API płatności - moduł pl.fullstackdeveloper.payments
 nie jest nawet czytelny (readable) z tego modułu.
 */
open module pl.fullstackdeveloper.orders {

    requires pl.fullstackdeveloper.common;
    requires pl.fullstackdeveloper.payments.api;

    requires spring.beans;
    requires spring.context;
    requires spring.web;
    requires jakarta.validation;

}
