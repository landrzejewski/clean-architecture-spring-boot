/**
 * Moduł aplikacyjny "orders". Jawnie deklarujemy dozwolone zależności: tylko API płatności i common.
 */
@ApplicationModule(displayName = "Orders", allowedDependencies = {"payments::api", "common"})
package pl.fullstackdeveloper.orders;

import org.springframework.modulith.ApplicationModule;
