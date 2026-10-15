/*
 Moduł JPMS: eksportujemy tylko to, czego potrzebują inne moduły w czasie kompilacji.
 Aspekty (common.aop) nie są eksportowane - nikt nie odwołuje się do nich w kodzie, Spring znajduje je
 przez refleksję, dlatego pakiet jest jedynie otwarty (opens) do refleksji.
 */
module pl.fullstackdeveloper.common {

    exports pl.fullstackdeveloper.common;
    exports pl.fullstackdeveloper.common.annotations;
    exports pl.fullstackdeveloper.common.web;

    requires spring.context;
    requires spring.core;
    requires spring.tx;
    requires spring.web;
    requires spring.webmvc;
    requires org.aspectj.weaver;
    requires org.slf4j;

    opens pl.fullstackdeveloper.common.aop;

}
